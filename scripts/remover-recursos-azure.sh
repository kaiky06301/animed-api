#!/usr/bin/env bash
# Remove o resource group inteiro (ACR + os dois ACIs).
#   export RG=rg-animed-sprint3
#   bash scripts/remover-recursos-azure.sh
set -euo pipefail
RG="${RG:-rg-animed-sprint3}"
az group delete --name "$RG" --yes --no-wait
echo "Exclusão do grupo $RG iniciada."
