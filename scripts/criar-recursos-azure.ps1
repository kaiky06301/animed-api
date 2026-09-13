# =====================================================
# Mesmo fluxo do .sh, para PowerShell no Windows.
# Todos os recursos via Azure CLI (nada pelo Portal).
#
#   $env:ORACLE_PASSWORD = '...'
#   $env:APP_USER_PASSWORD = '...'
#   $env:ANIMED_JWT_SECRET = '...'
#   .\scripts\criar-recursos-azure.ps1
# =====================================================
$ErrorActionPreference = "Stop"

if (-not $env:ORACLE_PASSWORD -or -not $env:APP_USER_PASSWORD -or -not $env:ANIMED_JWT_SECRET) {
    throw "Defina ORACLE_PASSWORD, APP_USER_PASSWORD e ANIMED_JWT_SECRET."
}

$Location = if ($env:LOCATION) { $env:LOCATION } else { "brazilsouth" }
$Rg = if ($env:RG) { $env:RG } else { "rg-animed-sprint3" }
$Suffix = Get-Random -Minimum 1000 -Maximum 9999
$Acr = "acranimed$Suffix"
$AciOracle = "aci-animed-oracle"
$AciApi = "aci-animed-api"
$DnsOracle = "animed-oracle-$Suffix"
$DnsApi = "animed-api-$Suffix"
$AppUser = if ($env:APP_USER) { $env:APP_USER } else { "clyvo" }
$OracleDatabase = "CLYVOVET"

Write-Host "==> Resource group"
az group create --name $Rg --location $Location | Out-Null

Write-Host "==> Azure Container Registry"
az acr create --resource-group $Rg --name $Acr --sku Basic --admin-enabled true --location $Location | Out-Null
$AcrServer = az acr show --name $Acr --query loginServer -o tsv
$AcrUser = az acr credential show --name $Acr --query username -o tsv
$AcrPass = az acr credential show --name $Acr --query passwords[0].value -o tsv

Write-Host "==> Importa imagem do Oracle para o ACR"
az acr import --name $Acr --source docker.io/gvenzl/oracle-xe:21-slim --image oracle-xe:21 --force | Out-Null

Write-Host "==> Build + push da API no ACR (docker; ACR Tasks e bloqueado no Azure for Students)"
az acr login --name $Acr
docker build -t animed-api:1.0 .
docker tag animed-api:1.0 "$AcrServer/animed-api:1.0"
docker push "$AcrServer/animed-api:1.0"

Write-Host "==> ACI do Oracle"
az container create `
  --resource-group $Rg `
  --name $AciOracle `
  --image "$AcrServer/oracle-xe:21" `
  --registry-login-server $AcrServer `
  --registry-username $AcrUser `
  --registry-password $AcrPass `
  --cpu 2 --memory 3.5 `
  --ports 1521 `
  --os-type Linux `
  --dns-name-label $DnsOracle `
  --environment-variables `
    ORACLE_PASSWORD=$env:ORACLE_PASSWORD `
    ORACLE_DATABASE=$OracleDatabase `
    APP_USER=$AppUser `
    APP_USER_PASSWORD=$env:APP_USER_PASSWORD `
  --location $Location | Out-Null

$OracleFqdn = az container show --resource-group $Rg --name $AciOracle --query ipAddress.fqdn -o tsv
$Jdbc = "jdbc:oracle:thin:@//${OracleFqdn}:1521/${OracleDatabase}"

Write-Host "==> Aguardando o Oracle (4 minutos)..."
Start-Sleep -Seconds 240

Write-Host "==> ACI da API"
az container create `
  --resource-group $Rg `
  --name $AciApi `
  --image "$AcrServer/animed-api:1.0" `
  --registry-login-server $AcrServer `
  --registry-username $AcrUser `
  --registry-password $AcrPass `
  --cpu 1 --memory 1.5 `
  --ports 8080 `
  --os-type Linux `
  --dns-name-label $DnsApi `
  --environment-variables `
    SPRING_PROFILES_ACTIVE=oracle `
    SPRING_DATASOURCE_URL=$Jdbc `
    SPRING_DATASOURCE_USERNAME=$AppUser `
    SPRING_DATASOURCE_PASSWORD=$env:APP_USER_PASSWORD `
    ORACLE_SCHEMA=CLYVO `
    ANIMED_JWT_SECRET=$env:ANIMED_JWT_SECRET `
    ANIMED_SEED_USUARIOS=true `
  --location $Location | Out-Null

$ApiFqdn = az container show --resource-group $Rg --name $AciApi --query ipAddress.fqdn -o tsv

Write-Host ""
Write-Host "===== Recursos criados ====="
Write-Host "Resource group : $Rg"
Write-Host "ACR            : $AcrServer"
Write-Host "Oracle ACI     : ${OracleFqdn}:1521"
Write-Host "API ACI        : http://${ApiFqdn}:8080"
Write-Host "Swagger        : http://${ApiFqdn}:8080/swagger-ui.html"
