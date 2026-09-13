#!/usr/bin/env bash
# =====================================================
# Cria TODOS os recursos da Sprint 3 (opção ACR + ACI)
# via Azure CLI: Resource Group, ACR, ACI do Oracle e
# ACI da API. Nada é criado pelo Portal.
#
# Uso (Azure Cloud Shell ou terminal com az + docker):
#   export ORACLE_PASSWORD='...'
#   export APP_USER_PASSWORD='...'
#   export ANIMED_JWT_SECRET='...'
#   bash scripts/criar-recursos-azure.sh
# =====================================================
set -euo pipefail

if [[ -z "${ORACLE_PASSWORD:-}" || -z "${APP_USER_PASSWORD:-}" || -z "${ANIMED_JWT_SECRET:-}" ]]; then
  echo "Defina ORACLE_PASSWORD, APP_USER_PASSWORD e ANIMED_JWT_SECRET antes de rodar."
  exit 1
fi

LOCATION="${LOCATION:-brazilsouth}"
RG="${RG:-rg-animed-sprint3}"
SUFFIX="${SUFFIX:-$RANDOM}"
ACR="acranimed${SUFFIX}"
ACI_ORACLE="aci-animed-oracle"
ACI_API="aci-animed-api"
DNS_ORACLE="animed-oracle-${SUFFIX}"
DNS_API="animed-api-${SUFFIX}"
APP_USER="${APP_USER:-clyvo}"
ORACLE_DATABASE="${ORACLE_DATABASE:-CLYVOVET}"

echo "==> Resource group"
az group create --name "$RG" --location "$LOCATION"

echo "==> Azure Container Registry"
az acr create --resource-group "$RG" --name "$ACR" --sku Basic --admin-enabled true --location "$LOCATION"
ACR_SERVER=$(az acr show --name "$ACR" --query loginServer -o tsv)
ACR_USER=$(az acr credential show --name "$ACR" --query username -o tsv)
ACR_PASS=$(az acr credential show --name "$ACR" --query passwords[0].value -o tsv)

echo "==> Importa imagem do Oracle para o ACR"
az acr import --name "$ACR" --source docker.io/gvenzl/oracle-xe:21-slim --image oracle-xe:21 --force

echo "==> Build + push da API no ACR (docker; ACR Tasks e bloqueado no Azure for Students)"
az acr login --name "$ACR"
docker build -t animed-api:1.0 .
docker tag animed-api:1.0 "${ACR_SERVER}/animed-api:1.0"
docker push "${ACR_SERVER}/animed-api:1.0"

echo "==> ACI do Oracle (container do banco)"
az container create \
  --resource-group "$RG" \
  --name "$ACI_ORACLE" \
  --image "${ACR_SERVER}/oracle-xe:21" \
  --registry-login-server "$ACR_SERVER" \
  --registry-username "$ACR_USER" \
  --registry-password "$ACR_PASS" \
  --cpu 2 --memory 3.5 \
  --ports 1521 \
  --os-type Linux \
  --dns-name-label "$DNS_ORACLE" \
  --environment-variables \
    ORACLE_PASSWORD="$ORACLE_PASSWORD" \
    ORACLE_DATABASE="$ORACLE_DATABASE" \
    APP_USER="$APP_USER" \
    APP_USER_PASSWORD="$APP_USER_PASSWORD" \
  --location "$LOCATION"

ORACLE_FQDN=$(az container show --resource-group "$RG" --name "$ACI_ORACLE" --query ipAddress.fqdn -o tsv)
JDBC="jdbc:oracle:thin:@//${ORACLE_FQDN}:1521/${ORACLE_DATABASE}"

echo "==> Aguardando o Oracle ficar pronto (4 minutos)..."
sleep 240

echo "==> ACI da API (container da aplicação, usuário não-root da imagem)"
az container create \
  --resource-group "$RG" \
  --name "$ACI_API" \
  --image "${ACR_SERVER}/animed-api:1.0" \
  --registry-login-server "$ACR_SERVER" \
  --registry-username "$ACR_USER" \
  --registry-password "$ACR_PASS" \
  --cpu 1 --memory 1.5 \
  --ports 8080 \
  --os-type Linux \
  --dns-name-label "$DNS_API" \
  --environment-variables \
    SPRING_PROFILES_ACTIVE=oracle \
    SPRING_DATASOURCE_URL="$JDBC" \
    SPRING_DATASOURCE_USERNAME="$APP_USER" \
    SPRING_DATASOURCE_PASSWORD="$APP_USER_PASSWORD" \
    ORACLE_SCHEMA=CLYVO \
    ANIMED_JWT_SECRET="$ANIMED_JWT_SECRET" \
    ANIMED_SEED_USUARIOS=true \
  --location "$LOCATION"

API_FQDN=$(az container show --resource-group "$RG" --name "$ACI_API" --query ipAddress.fqdn -o tsv)

echo
echo "===== Recursos criados ====="
echo "Resource group : $RG"
echo "ACR            : $ACR_SERVER"
echo "Oracle ACI     : $ORACLE_FQDN:1521"
echo "API ACI        : http://${API_FQDN}:8080"
echo "Swagger        : http://${API_FQDN}:8080/swagger-ui.html"
echo "Health         : http://${API_FQDN}:8080/actuator/health"
echo "Guarde as senhas fora do Git."
