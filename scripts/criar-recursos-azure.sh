#!/usr/bin/env bash
# =====================================================================
# Cria TODOS os recursos da Sprint 3 (opção 1: ACR + ACI) via Azure CLI.
# Nada é criado pelo Portal.
#
#   Resource Group -> ACR -> imagem do Oracle -> imagem da API
#                        -> ACI do banco -> ACI da aplicação
#
# Uso:
#   export ORACLE_PASSWORD='...'      # senha do SYS/SYSTEM do Oracle
#   export APP_USER_PASSWORD='...'    # senha do usuário da aplicação
#   export ANIMED_JWT_SECRET="$(openssl rand -base64 48)"   # PRECISA ser Base64
#   bash scripts/criar-recursos-azure.sh
#
# As senhas nunca entram no Git: ficam em variável de ambiente e sobem
# como --secure-environment-variables (não aparecem no `az container show`).
# =====================================================================
set -euo pipefail

if [[ -z "${ORACLE_PASSWORD:-}" || -z "${APP_USER_PASSWORD:-}" || -z "${ANIMED_JWT_SECRET:-}" ]]; then
  echo "Defina ORACLE_PASSWORD, APP_USER_PASSWORD e ANIMED_JWT_SECRET antes de rodar."
  exit 1
fi

# O JwtService faz Decoders.BASE64.decode(segredo): um valor com '-' ou '_'
# derruba a aplicação na subida, antes mesmo de tocar no banco.
if ! printf '%s' "$ANIMED_JWT_SECRET" | grep -Eq '^[A-Za-z0-9+/]+={0,2}$'; then
  echo "ANIMED_JWT_SECRET precisa ser Base64 puro. Gere com: openssl rand -base64 48"
  exit 1
fi

LOCATION="${LOCATION:-brazilsouth}"
RG="${RG:-rg-animed-sprint3}"
SUFFIX="${SUFFIX:-566067}"
ACR="${ACR:-acranimed${SUFFIX}}"
ACI_ORACLE="aci-animed-oracle"
ACI_API="aci-animed-api"
DNS_ORACLE="animed-oracle-${SUFFIX}"
DNS_API="animed-api-${SUFFIX}"
APP_USER="${APP_USER:-clyvo}"
ORACLE_DATABASE="${ORACLE_DATABASE:-CLYVOVET}"
ORACLE_SCHEMA="${ORACLE_SCHEMA:-CLYVO}"

echo "==> Registrando os providers (só faz efeito na primeira vez)"
az provider register --namespace Microsoft.ContainerRegistry -o none
az provider register --namespace Microsoft.ContainerInstance -o none
until [[ "$(az provider show -n Microsoft.ContainerRegistry --query registrationState -o tsv)" == "Registered" \
      && "$(az provider show -n Microsoft.ContainerInstance --query registrationState -o tsv)" == "Registered" ]]; do
  echo "    aguardando registro..."
  sleep 20
done

echo "==> Resource group"
az group create --name "$RG" --location "$LOCATION" -o none

echo "==> Azure Container Registry"
az acr create --resource-group "$RG" --name "$ACR" --sku Basic \
  --admin-enabled true --location "$LOCATION" -o none
ACR_SERVER=$(az acr show --name "$ACR" --query loginServer -o tsv)
ACR_USER=$(az acr credential show --name "$ACR" --query username -o tsv)
ACR_PASS=$(az acr credential show --name "$ACR" --query "passwords[0].value" -o tsv)

echo "==> Importando a imagem do Oracle para o ACR"
az acr import --name "$ACR" --source docker.io/gvenzl/oracle-xe:21-slim --image oracle-xe:21 --force -o none

echo "==> Build da imagem da API (kaniko dentro da Azure — ver o script)"
ACR="$ACR" RG="$RG" LOCATION="$LOCATION" bash "$(dirname "$0")/build-imagem-kaniko.sh"

echo "==> ACI do banco (Oracle em container)"
az container create \
  --resource-group "$RG" \
  --name "$ACI_ORACLE" \
  --image "${ACR_SERVER}/oracle-xe:21" \
  --registry-login-server "$ACR_SERVER" \
  --registry-username "$ACR_USER" \
  --registry-password "$ACR_PASS" \
  --cpu 2 --memory 4 \
  --ports 1521 \
  --os-type Linux \
  --dns-name-label "$DNS_ORACLE" \
  --restart-policy OnFailure \
  --location "$LOCATION" \
  --environment-variables ORACLE_DATABASE="$ORACLE_DATABASE" APP_USER="$APP_USER" \
  --secure-environment-variables ORACLE_PASSWORD="$ORACLE_PASSWORD" APP_USER_PASSWORD="$APP_USER_PASSWORD" \
  -o none

ORACLE_FQDN=$(az container show --resource-group "$RG" --name "$ACI_ORACLE" --query ipAddress.fqdn -o tsv)
JDBC="jdbc:oracle:thin:@//${ORACLE_FQDN}:1521/${ORACLE_DATABASE}"

echo "==> Aguardando o Oracle aceitar conexão em ${ORACLE_FQDN}:1521"
# O XE leva alguns minutos para criar o banco e o usuário da aplicação.
# Subir a API antes disso só gera CrashLoop com o histórico do Flyway sujo.
until (echo > /dev/tcp/"${ORACLE_FQDN}"/1521) >/dev/null 2>&1; do
  echo "    ainda subindo..."
  sleep 20
done
echo "    porta aberta; dando mais 60s para o usuário ${APP_USER} ser criado"
sleep 60

echo "==> ACI da aplicação (container roda com o usuário 'animed', não root)"
az container create \
  --resource-group "$RG" \
  --name "$ACI_API" \
  --image "${ACR_SERVER}/animed-api:1.0" \
  --registry-login-server "$ACR_SERVER" \
  --registry-username "$ACR_USER" \
  --registry-password "$ACR_PASS" \
  --cpu 1 --memory 2 \
  --ports 8080 \
  --os-type Linux \
  --dns-name-label "$DNS_API" \
  --restart-policy OnFailure \
  --location "$LOCATION" \
  --environment-variables \
    SPRING_PROFILES_ACTIVE=oracle \
    SPRING_DATASOURCE_URL="$JDBC" \
    SPRING_DATASOURCE_USERNAME="$APP_USER" \
    ORACLE_SCHEMA="$ORACLE_SCHEMA" \
  --secure-environment-variables \
    SPRING_DATASOURCE_PASSWORD="$APP_USER_PASSWORD" \
    ANIMED_JWT_SECRET="$ANIMED_JWT_SECRET" \
  -o none

API_FQDN=$(az container show --resource-group "$RG" --name "$ACI_API" --query ipAddress.fqdn -o tsv)

echo "==> Aguardando a API responder"
until curl -sf -m 10 "http://${API_FQDN}:8080/actuator/health" >/dev/null 2>&1; do
  echo "    subindo (Flyway aplica as 12 migrations na primeira vez)..."
  sleep 20
done

echo
echo "===== Recursos criados ====="
echo "Resource group : $RG"
echo "ACR            : $ACR_SERVER"
echo "Oracle (ACI)   : ${ORACLE_FQDN}:1521  service ${ORACLE_DATABASE}"
echo "API (ACI)      : http://${API_FQDN}:8080"
echo "Swagger        : http://${API_FQDN}:8080/swagger-ui.html"
echo "Painel web     : http://${API_FQDN}:8080/login"
echo "Health         : http://${API_FQDN}:8080/actuator/health"
