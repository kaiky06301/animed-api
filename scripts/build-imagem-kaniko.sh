#!/usr/bin/env bash
# =====================================================================
# Build da imagem da API DENTRO da Azure, sem Docker na máquina.
#
# Por que não `docker build`: a subscription Azure for Students da FIAP
# bloqueia ACR Tasks (`az acr build` devolve TasksOperationsNotAllowed)
# e nem toda máquina da equipe tem Docker Desktop. O kaniko resolve os
# dois casos: é um builder de imagem que roda como container comum no
# ACI, clona o repositório do GitHub, executa o Dockerfile e dá push
# direto no ACR.
#
# Uso:
#   export ACR=acranimed566067
#   export RG=rg-animed-sprint3
#   bash scripts/build-imagem-kaniko.sh
# =====================================================================
set -euo pipefail

ACR="${ACR:?defina ACR (ex.: acranimed566067)}"
RG="${RG:-rg-animed-sprint3}"
LOCATION="${LOCATION:-brazilsouth}"
REPO_GIT="${REPO_GIT:-github.com/kaiky06301/animed-api.git}"
BRANCH="${BRANCH:-main}"
IMAGEM="${IMAGEM:-animed-api:1.0}"
BUILDER="aci-kaniko-build"

ACR_SERVER=$(az acr show --name "$ACR" --query loginServer -o tsv)
ACR_USER=$(az acr credential show --name "$ACR" --query username -o tsv)
ACR_PASS=$(az acr credential show --name "$ACR" --query "passwords[0].value" -o tsv)

# O kaniko lê as credenciais do registry de /kaniko/.docker/config.json.
# Mandamos o arquivo em base64 para a senha não aparecer solta na linha
# de comando do container.
CONFIG_B64=$(printf '{"auths":{"%s":{"auth":"%s"}}}' \
  "$ACR_SERVER" "$(printf '%s:%s' "$ACR_USER" "$ACR_PASS" | base64 -w0)" | base64 -w0)

echo "==> Removendo build anterior, se existir"
az container delete --resource-group "$RG" --name "$BUILDER" --yes -o none 2>/dev/null || true

echo "==> Buildando $IMAGEM a partir de $REPO_GIT ($BRANCH)"
az container create \
  --resource-group "$RG" \
  --name "$BUILDER" \
  --image gcr.io/kaniko-project/executor:v1.23.2-debug \
  --os-type Linux \
  --cpu 2 --memory 4 \
  --restart-policy Never \
  --location "$LOCATION" \
  --command-line "/busybox/sh -c 'mkdir -p /kaniko/.docker && echo ${CONFIG_B64} | base64 -d > /kaniko/.docker/config.json && /kaniko/executor --context=git://${REPO_GIT}#refs/heads/${BRANCH} --dockerfile=Dockerfile --destination=${ACR_SERVER}/${IMAGEM} --cache=false --verbosity=info'" \
  -o none

echo "==> Aguardando o build terminar"
while [ "$(az container show -g "$RG" -n "$BUILDER" --query "containers[0].instanceView.currentState.state" -o tsv)" = "Running" ]; do
  sleep 15
done

ESTADO=$(az container show -g "$RG" -n "$BUILDER" --query "containers[0].instanceView.currentState.exitCode" -o tsv)
az container logs --resource-group "$RG" --name "$BUILDER" | tail -5

if [ "$ESTADO" != "0" ]; then
  echo "Build FALHOU (exit $ESTADO). Veja o log acima."
  exit 1
fi

echo "==> Imagem publicada: ${ACR_SERVER}/${IMAGEM}"
az acr repository show-tags --name "$ACR" --repository "${IMAGEM%%:*}" -o table

# O builder já cumpriu o papel; sem remover, ele segura 2 cores da cota
# do Azure for Students (limite de 6) e o ACI da API não sobe.
az container delete --resource-group "$RG" --name "$BUILDER" --yes -o none
echo "==> Builder removido (libera cota de CPU para os ACIs)"
