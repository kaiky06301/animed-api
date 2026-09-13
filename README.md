# Animed API — Sprint 3 (DevOps Tools & Cloud Computing)

API REST + painel web de **cuidado contínuo e gamificação para pets**. Challenge FIAP 2026.

**Opção de entrega:** ACR + ACI (app e banco **100% containerizados**).  
Nada de App Service. Nada de banco PaaS misturado com container.

| | |
|---|---|
| Repositório | https://github.com/kaiky06301/animed-api |
| Diagrama | [docs/diagrama-arquitetura.html](docs/diagrama-arquitetura.html) |
| DDL do banco | [script_bd.sql](script_bd.sql) |
| PDF da entrega | [docs/entrega-pdf.html](docs/entrega-pdf.html) |

---

## 1. O que a aplicação faz

A Animed transforma a saúde do pet de um modelo **episódico** (só vai ao vet na emergência) em uma jornada **contínua**: o tutor cadastra o pet, registra vacina, consulta e medicação, e ganha pontos. Os pontos sobem o nível (Básico → Cuidador → Premium) e liberam desconto em pet shops parceiros.

O núcleo persistido no Oracle é **Tutor + Pet** (1:N). As demais tabelas (vacina, consulta, pontuação) giram em torno desse núcleo.

CRUD completo nas duas tabelas: `POST/GET/PUT/DELETE` em `/api/tutores` e `/api/pets`. Também há painel web em `/login` (Thymeleaf).

## 2. Benefícios para o negócio

| Quem | Problema hoje | O que a Animed entrega |
|------|----------------|------------------------|
| Tutor | Esquece vacina e retorno | Rotina de cuidado recompensada com pontos |
| Pet | Cuidado só na crise | Mais check-up e vacina em dia |
| Clínica | Paciente some depois da consulta | Recorrência e histórico longitudinal |
| Pet shop | Compra avulsa | Tráfego de tutor com desconto progressivo |
| Animed | Sem recorrência | Comissão na venda + assinatura do parceiro |

---

## 3. Stack e opção de nuvem

| Camada | Tecnologia |
|--------|------------|
| Linguagem | Java 17 |
| API / Web | Spring Boot 3.3.4 + JPA + Flyway + JWT + Thymeleaf + Swagger |
| Banco (nuvem) | Oracle XE 21 **em container** (ACI) |
| Imagens | Azure Container Registry |
| Execução | Azure Container Instance (API + Oracle) |
| Build | Docker + Azure CLI |

H2 existe **somente** para `mvn test` na máquina. Entrega = Oracle no ACI.

> Azure for Students **bloqueia ACR Tasks** (`az acr build`). O caminho oficial desta entrega é `docker build` + `docker push`.

---

## 4. Arquitetura (recursos e fluxo)

Abra [docs/diagrama-arquitetura.html](docs/diagrama-arquitetura.html) e tire o print.

```
 Desenvolvedor                 Azure
 ┌───────────┐   push    ┌──────────┐
 │  GitHub   │──────────▶│   ACR    │  imagens: animed-api e oracle-xe
 └───────────┘           └────┬─────┘
                              │ pull
                     ┌────────┴────────┐
                     ▼                 ▼
              ┌────────────┐    ┌─────────────┐
              │ ACI da API │───▶│ ACI Oracle  │
              │ (não-root) │    │ porta 1521  │
              └─────┬──────┘    └─────────────┘
                    │
              Tutor / Vet (Swagger e /login :8080)
```

- **GitHub** — código e este README (o vídeo clona daqui).
- **ACR** — guarda as duas imagens Docker.
- **ACI da API** — sobe o container da Animed com usuário `animed` (não é root).
- **ACI Oracle** — banco em container na nuvem. A API grava Tutor e Pet aqui.
- Senha, JDBC e JWT entram por **variável de ambiente**, nunca pelo código.

---

## 5. Como testar e publicar (siga esta ordem no vídeo)

### 5.1 Pré-requisitos

- Git, Java 17, Azure CLI (`az login`)
- Docker Desktop (obrigatório neste subscription: o `az acr build` é bloqueado)
- Conta Azure com permissão para criar Resource Group, ACR e ACI

### 5.2 Clonar o repositório (obrigatório)

```bash
git clone https://github.com/kaiky06301/animed-api.git
cd animed-api
```

### 5.3 Rodar os testes da solução

Windows:

```bat
mvnw.cmd test
```

Linux / Mac:

```bash
./mvnw test
```

Os testes JUnit sobem com H2 em memória. Eles **não** usam o Oracle da nuvem.

### 5.4 Login na Azure e senhas (não vão para o Git)

```bash
az login
az account show
```

```bash
export ORACLE_PASSWORD='TroqueEstaSenha1'
export APP_USER_PASSWORD='TroqueEstaSenha1'
export ANIMED_JWT_SECRET='animed-segredo-base64-nao-commitar'
export ACR_SERVER='acranimedXXXX.azurecr.io'
```

PowerShell:

```powershell
az login
$env:ORACLE_PASSWORD = 'TroqueEstaSenha1'
$env:APP_USER_PASSWORD = 'TroqueEstaSenha1'
$env:ANIMED_JWT_SECRET = 'animed-segredo-base64-nao-commitar'
$env:ACR_SERVER = 'acranimedXXXX.azurecr.io'
```

### 5.5 Criar os recursos na nuvem (tudo via Azure CLI)

**Caminho A — um script só:**

```powershell
.\scripts\criar-recursos-azure.ps1
```

Cloud Shell / bash:

```bash
bash scripts/criar-recursos-azure.sh
```

**Caminho B — comandos um a um.** Troque `XXXX` por um número único:

```bash
az group create --name rg-animed-sprint3 --location brazilsouth

az acr create --resource-group rg-animed-sprint3 --name acranimedXXXX --sku Basic --admin-enabled true

az acr import --name acranimedXXXX --source docker.io/gvenzl/oracle-xe:21-slim --image oracle-xe:21 --force

az acr login --name acranimedXXXX
docker build -t animed-api:1.0 .
docker tag animed-api:1.0 acranimedXXXX.azurecr.io/animed-api:1.0
docker push acranimedXXXX.azurecr.io/animed-api:1.0
```

Ou: `export ACR_SERVER=acranimedXXXX.azurecr.io` e `bash scripts/docker-build-push.sh`.

Subir o banco e a API no ACI (usuário/senha do ACR: `az acr credential show --name acranimedXXXX`):

```bash
az container create \
  --resource-group rg-animed-sprint3 \
  --name aci-animed-oracle \
  --image acranimedXXXX.azurecr.io/oracle-xe:21 \
  --registry-login-server acranimedXXXX.azurecr.io \
  --registry-username <usuario-acr> \
  --registry-password <senha-acr> \
  --cpu 2 --memory 3.5 --ports 1521 --os-type Linux \
  --dns-name-label animed-oracle-XXXX \
  --environment-variables \
    ORACLE_PASSWORD=$ORACLE_PASSWORD \
    ORACLE_DATABASE=CLYVOVET \
    APP_USER=clyvo \
    APP_USER_PASSWORD=$APP_USER_PASSWORD

# Espere ~4 minutos o Oracle ficar pronto. Depois:

az container create \
  --resource-group rg-animed-sprint3 \
  --name aci-animed-api \
  --image acranimedXXXX.azurecr.io/animed-api:1.0 \
  --registry-login-server acranimedXXXX.azurecr.io \
  --registry-username <usuario-acr> \
  --registry-password <senha-acr> \
  --cpu 1 --memory 1.5 --ports 8080 --os-type Linux \
  --dns-name-label animed-api-XXXX \
  --environment-variables \
    SPRING_PROFILES_ACTIVE=oracle \
    SPRING_DATASOURCE_URL=jdbc:oracle:thin:@//animed-oracle-XXXX.brazilsouth.azurecontainer.io:1521/CLYVOVET \
    SPRING_DATASOURCE_USERNAME=clyvo \
    SPRING_DATASOURCE_PASSWORD=$APP_USER_PASSWORD \
    ORACLE_SCHEMA=CLYVO \
    ANIMED_JWT_SECRET=$ANIMED_JWT_SECRET
```

O container da API **não roda como root**: o `Dockerfile` cria o usuário `animed` e usa `USER animed`.

### 5.6 Conferir os recursos no Portal

No Portal do Azure, abra o grupo `rg-animed-sprint3` e mostre:

- Azure Container Registry com as imagens `animed-api:1.0` e `oracle-xe:21`
- Container Instance `aci-animed-oracle` (Running)
- Container Instance `aci-animed-api` (Running)

```
http://<fqdn-da-api>:8080/login
http://<fqdn-da-api>:8080/swagger-ui.html
http://<fqdn-da-api>:8080/actuator/health
```

```bash
az container show -g rg-animed-sprint3 -n aci-animed-api --query ipAddress.fqdn -o tsv
az container show -g rg-animed-sprint3 -n aci-animed-oracle --query ipAddress.fqdn -o tsv
```

### 5.7 CRUD + evidência no banco (sem corte no vídeo)

Contas de demo (criadas na subida da API):

| Perfil | E-mail | Senha |
|--------|--------|--------|
| Veterinário | doutor@animed.com.br | animed123 |
| Tutor | tutor@animed.com.br | animed123 |

1. Abra `/login` **ou** o Swagger (`POST /api/auth/login` com o doutor).
2. **Inserir** um tutor (`POST /api/tutores`) e um pet (`POST /api/pets`) ligado a esse tutor.
3. No Oracle, rode o `SELECT` e mostre as linhas novas.
4. **Atualizar** tutor e pet (`PUT`). SELECT de novo.
5. **Consultar** (`GET` e SELECT).
6. **Excluir** o pet e o tutor de teste (`DELETE`). SELECT mostrando que sumiram.

SELECT no container do Oracle:

```bash
az container exec -g rg-animed-sprint3 -n aci-animed-oracle --exec-command \
  "sqlplus -s clyvo/${APP_USER_PASSWORD}@//localhost:1521/CLYVOVET"
```

```sql
SELECT ID_TUTOR, NOME, EMAIL, NIVEL FROM TB_TUTOR ORDER BY ID_TUTOR;
SELECT ID_PET, NOME, ESPECIE, RACA, ID_TUTOR FROM TB_PET ORDER BY ID_PET;
```

Ou conecte o DBeaver/SQL Developer no FQDN do ACI Oracle, porta `1521`, service `CLYVOVET`, usuário `clyvo`.

O Flyway já aplica as migrations e o seed (Marina, Carlos, Thor, Mia, …) na primeira subida. Isso cobre as **2+ linhas significativas** nas tabelas-núcleo. O CRUD do vídeo cria/edita/apaga **outras** linhas para a câmera.

---

## 6. Scripts entregues

| Arquivo | Para quê |
|---------|----------|
| `Dockerfile` | Build da API, usuário `animed` (não-root) |
| `docker-compose.yml` | App + Oracle em container (mesmo desenho, na máquina) |
| `scripts/criar-recursos-azure.sh` | Resource group + ACR + 2 ACIs |
| `scripts/criar-recursos-azure.ps1` | Idem no Windows |
| `scripts/docker-build-push.sh` | `docker build`, `tag` e `push` no ACR |
| `scripts/remover-recursos-azure.sh` | Apaga o resource group |
| `script_bd.sql` | DDL comentado + carga mínima de Tutor e Pet |

---

## 7. Rotas usadas na correção

| Método | Rota | Uso no vídeo |
|--------|------|----------------|
| POST | `/api/auth/login` | Pegar o JWT |
| POST | `/api/tutores` | Inserir tutor |
| GET | `/api/tutores` | Consultar tutores |
| PUT | `/api/tutores/{id}` | Atualizar tutor |
| DELETE | `/api/tutores/{id}` | Excluir tutor |
| POST | `/api/pets` | Inserir pet (FK do tutor) |
| GET | `/api/pets` | Consultar pets |
| PUT | `/api/pets/{id}` | Atualizar pet |
| DELETE | `/api/pets/{id}` | Excluir pet |

Painel web: `/login`. Swagger: `/swagger-ui.html`.

---

## 8. Equipe

| Nome | RM |
|------|------|
| Erick Bernardes Bradaschia | 565733 |
| Gabriel Santos Claudino | 564054 |
| Kaiky de Oliveira Silva | 566067 |
| Lucas Fortes de Lima | 559523 |
| Jonathan Moreira Gomes | 565060 |

Turma 2TDS — FIAP.

O PDF da entrega tem **somente** nomes, RMs, GitHub e YouTube. O resto está neste README.
