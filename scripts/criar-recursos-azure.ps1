# =====================================================================
# Mesmo fluxo do .sh, para PowerShell no Windows.
# Todos os recursos via Azure CLI - nada pelo Portal.
#
#   $env:ORACLE_PASSWORD   = '...'
#   $env:APP_USER_PASSWORD = '...'
#   $env:ANIMED_JWT_SECRET = '...'   # PRECISA ser Base64 (veja o README)
#   .\scripts\criar-recursos-azure.ps1
#
# As senhas nunca entram no Git: ficam em variavel de ambiente e sobem
# como --secure-environment-variables (nao aparecem no `az container show`).
# =====================================================================
$ErrorActionPreference = "Stop"

if (-not $env:ORACLE_PASSWORD -or -not $env:APP_USER_PASSWORD -or -not $env:ANIMED_JWT_SECRET) {
    throw "Defina ORACLE_PASSWORD, APP_USER_PASSWORD e ANIMED_JWT_SECRET."
}

# O JwtService faz Decoders.BASE64.decode(segredo): um valor com '-' ou '_'
# derruba a aplicacao na subida, antes mesmo de tocar no banco.
if ($env:ANIMED_JWT_SECRET -notmatch '^[A-Za-z0-9+/]+={0,2}$') {
    throw "ANIMED_JWT_SECRET precisa ser Base64 puro. Veja como gerar no README."
}

$Location       = if ($env:LOCATION) { $env:LOCATION } else { "brazilsouth" }
$Rg             = if ($env:RG) { $env:RG } else { "rg-animed-sprint3" }
$Suffix         = if ($env:SUFFIX) { $env:SUFFIX } else { "566067" }
$Acr            = if ($env:ACR) { $env:ACR } else { "acranimed$Suffix" }
$AciOracle      = "aci-animed-oracle"
$AciApi         = "aci-animed-api"
$DnsOracle      = "animed-oracle-$Suffix"
$DnsApi         = "animed-api-$Suffix"
$AppUser        = if ($env:APP_USER) { $env:APP_USER } else { "clyvo" }
$OracleDatabase = if ($env:ORACLE_DATABASE) { $env:ORACLE_DATABASE } else { "CLYVOVET" }
$OracleSchema   = if ($env:ORACLE_SCHEMA) { $env:ORACLE_SCHEMA } else { "CLYVO" }

Write-Host "==> Registrando os providers (so faz efeito na primeira vez)"
az provider register --namespace Microsoft.ContainerRegistry -o none
az provider register --namespace Microsoft.ContainerInstance -o none
do {
    $a = az provider show -n Microsoft.ContainerRegistry --query registrationState -o tsv
    $b = az provider show -n Microsoft.ContainerInstance --query registrationState -o tsv
    if ($a -ne "Registered" -or $b -ne "Registered") { Write-Host "    aguardando registro..."; Start-Sleep -Seconds 20 }
} while ($a -ne "Registered" -or $b -ne "Registered")

Write-Host "==> Resource group"
az group create --name $Rg --location $Location -o none

Write-Host "==> Azure Container Registry"
az acr create --resource-group $Rg --name $Acr --sku Basic --admin-enabled true --location $Location -o none
$AcrServer = az acr show --name $Acr --query loginServer -o tsv
$AcrUser   = az acr credential show --name $Acr --query username -o tsv
$AcrPass   = az acr credential show --name $Acr --query "passwords[0].value" -o tsv

Write-Host "==> Importando a imagem do Oracle para o ACR"
az acr import --name $Acr --source docker.io/gvenzl/oracle-xe:21-slim --image oracle-xe:21 --force -o none

# ---------------------------------------------------------------------
# Build da imagem da API DENTRO da Azure, com kaniko.
# Por que nao `docker build` / `az acr build`: o Azure for Students da FIAP
# bloqueia ACR Tasks (TasksOperationsNotAllowed) e nem toda maquina da
# equipe tem Docker Desktop. O kaniko e um builder que roda como container
# comum no ACI: clona o GitHub, executa o Dockerfile e da push no ACR.
# ---------------------------------------------------------------------
Write-Host "==> Build da imagem da API (kaniko no ACI)"
$RepoGit = if ($env:REPO_GIT) { $env:REPO_GIT } else { "github.com/kaiky06301/animed-api.git" }
$Branch  = if ($env:BRANCH) { $env:BRANCH } else { "main" }
$auth    = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("${AcrUser}:${AcrPass}"))
# Credenciais vao em base64 para a senha nao ficar solta na linha de comando.
$cfgJson = '{"auths":{"' + $AcrServer + '":{"auth":"' + $auth + '"}}}'
$cfgB64  = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($cfgJson))
$inner   = "mkdir -p /kaniko/.docker && echo $cfgB64 | base64 -d > /kaniko/.docker/config.json && /kaniko/executor --context=git://$RepoGit#refs/heads/$Branch --dockerfile=Dockerfile --destination=$AcrServer/animed-api:1.0 --cache=false --verbosity=info"

az container delete --resource-group $Rg --name aci-kaniko-build --yes -o none 2>$null
az container create --resource-group $Rg --name aci-kaniko-build `
  --image gcr.io/kaniko-project/executor:v1.23.2-debug `
  --os-type Linux --cpu 2 --memory 4 --restart-policy Never --location $Location `
  --command-line ("/busybox/sh -c '" + $inner + "'") -o none

while ((az container show -g $Rg -n aci-kaniko-build --query "containers[0].instanceView.currentState.state" -o tsv) -eq "Running") {
    Write-Host "    compilando..."
    Start-Sleep -Seconds 15
}
$exitCode = az container show -g $Rg -n aci-kaniko-build --query "containers[0].instanceView.currentState.exitCode" -o tsv
if ($exitCode -ne "0") {
    az container logs -g $Rg -n aci-kaniko-build
    throw "Build da imagem falhou (exit $exitCode)."
}
Write-Host "    imagem publicada: $AcrServer/animed-api:1.0"

# O builder ja cumpriu o papel; sem remover, segura 2 cores da cota do
# Azure for Students (limite de 6) e o ACI da API nao sobe.
az container delete --resource-group $Rg --name aci-kaniko-build --yes -o none

Write-Host "==> ACI do banco (Oracle em container)"
az container create `
  --resource-group $Rg --name $AciOracle `
  --image "$AcrServer/oracle-xe:21" `
  --registry-login-server $AcrServer --registry-username $AcrUser --registry-password $AcrPass `
  --cpu 2 --memory 4 --ports 1521 --os-type Linux `
  --dns-name-label $DnsOracle --restart-policy OnFailure --location $Location `
  --environment-variables ORACLE_DATABASE=$OracleDatabase APP_USER=$AppUser `
  --secure-environment-variables ORACLE_PASSWORD=$env:ORACLE_PASSWORD APP_USER_PASSWORD=$env:APP_USER_PASSWORD `
  -o none

$OracleFqdn = az container show --resource-group $Rg --name $AciOracle --query ipAddress.fqdn -o tsv
$Jdbc = "jdbc:oracle:thin:@//${OracleFqdn}:1521/${OracleDatabase}"

Write-Host "==> Aguardando o Oracle aceitar conexao em ${OracleFqdn}:1521"
# O XE leva alguns minutos para criar o banco e o usuario da aplicacao.
# Subir a API antes disso so gera CrashLoop com o historico do Flyway sujo.
do {
    $ok = Test-NetConnection -ComputerName $OracleFqdn -Port 1521 -WarningAction SilentlyContinue
    if (-not $ok.TcpTestSucceeded) { Write-Host "    ainda subindo..."; Start-Sleep -Seconds 20 }
} while (-not $ok.TcpTestSucceeded)
Write-Host "    porta aberta; dando mais 60s para o usuario $AppUser ser criado"
Start-Sleep -Seconds 60

Write-Host "==> ACI da aplicacao (container roda com o usuario 'animed', nao root)"
az container create `
  --resource-group $Rg --name $AciApi `
  --image "$AcrServer/animed-api:1.0" `
  --registry-login-server $AcrServer --registry-username $AcrUser --registry-password $AcrPass `
  --cpu 1 --memory 2 --ports 8080 --os-type Linux `
  --dns-name-label $DnsApi --restart-policy OnFailure --location $Location `
  --environment-variables SPRING_PROFILES_ACTIVE=oracle SPRING_DATASOURCE_URL=$Jdbc SPRING_DATASOURCE_USERNAME=$AppUser ORACLE_SCHEMA=$OracleSchema `
  --secure-environment-variables SPRING_DATASOURCE_PASSWORD=$env:APP_USER_PASSWORD ANIMED_JWT_SECRET=$env:ANIMED_JWT_SECRET `
  -o none

$ApiFqdn = az container show --resource-group $Rg --name $AciApi --query ipAddress.fqdn -o tsv

Write-Host "==> Aguardando a API responder"
do {
    Start-Sleep -Seconds 20
    try { $h = Invoke-RestMethod -Uri "http://${ApiFqdn}:8080/actuator/health" -TimeoutSec 10 }
    catch { $h = $null; Write-Host "    subindo (Flyway aplica as 12 migrations na primeira vez)..." }
} while (-not $h -or $h.status -ne "UP")

Write-Host ""
Write-Host "===== Recursos criados ====="
Write-Host "Resource group : $Rg"
Write-Host "ACR            : $AcrServer"
Write-Host "Oracle (ACI)   : ${OracleFqdn}:1521  service $OracleDatabase"
Write-Host "API (ACI)      : http://${ApiFqdn}:8080"
Write-Host "Swagger        : http://${ApiFqdn}:8080/swagger-ui.html"
Write-Host "Painel web     : http://${ApiFqdn}:8080/login"
Write-Host "Health         : http://${ApiFqdn}:8080/actuator/health"
