#!/usr/bin/env bash
# =====================================================
# Build local + push da imagem da API para o ACR
# Exige Docker Desktop e:  az acr login --name <acr>
#
#   export ACR_SERVER='acranimedXXXX.azurecr.io'
#   bash scripts/docker-build-push.sh
# =====================================================
set -euo pipefail

if [[ -z "${ACR_SERVER:-}" ]]; then
  echo "Defina ACR_SERVER (ex.: acranimed1234.azurecr.io)"
  exit 1
fi

docker build -t animed-api:1.0 .
docker tag animed-api:1.0 "${ACR_SERVER}/animed-api:1.0"
docker push "${ACR_SERVER}/animed-api:1.0"
echo "Imagem publicada: ${ACR_SERVER}/animed-api:1.0"
