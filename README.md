# Animed API

API REST + painel web de **cuidado contínuo e gamificação para pets**. Challenge FIAP 2026 — turma 2TDSR, empresa parceira Clyvo VET.

Este repositório atende a duas disciplinas da Sprint 3:

| Disciplina | Onde está |
|------------|-----------|
| **Java Advanced** — frontend, Spring Security, Flyway e fluxos completos | [seção A](#a--java-advanced), abaixo |
| **DevOps Tools & Cloud Computing** — publicação na Azure | [seção B](#b--devops-tools--cloud-computing), a partir do item 1 |

---

# A — Java Advanced

## O problema e a solução

A jornada de saúde do pet é **episódica e reativa**: o tutor procura a clínica quando o problema já aconteceu e some entre um evento e outro. O pet adoece por prevenção esquecida e a clínica perde recorrência.

O Animed transforma isso em cuidado contínuo por gamificação: **cada ato de cuidado vira pontos, os pontos viram nível, e o nível vira desconto real em pet shops parceiros**.

## Camada de visualização

Painel web em **Thymeleaf**, com folha de estilo própria (`static/css/animed.css`). Duas áreas distintas, cada uma com suas telas:

| Área | Telas |
|------|-------|
| **Tutor** | painel com pontos e nível, meus pets, agendamento, histórico de atendimentos |
| **Veterinário** | agenda do dia, pacientes, ficha clínica, conclusão de atendimento, cadastro de pessoas |

O login (`/login`) usa formulário próprio, e o redirecionamento após autenticar depende do perfil — `PainelPorPerfilHandler` manda o veterinário para `/painel/veterinario` e o tutor para `/painel/tutor`.

## Spring Security — dois perfis com permissões diferentes

`SecurityConfig` declara **duas cadeias de filtro** separadas por `@Order` e `securityMatcher`:

| Cadeia | Alcance | Autenticação |
|--------|---------|--------------|
| `apiFilterChain` (@Order 1) | `/api/**`, Swagger | JWT sem estado |
| `webFilterChain` (@Order 2) | painel web | formulário com sessão |

Proteção de rotas por perfil:

```java
.requestMatchers("/painel/veterinario/**").hasRole("DOUTOR")
.requestMatchers("/painel/tutor/**").hasRole("TUTOR")
.requestMatchers("/api/usuarios/**").hasRole("DOUTOR")
.anyRequest().authenticated()
```

Um tutor que digite `/painel/veterinario` na barra recebe 403 — a proteção não depende de esconder o link na tela.

## Flyway

Doze migrations versionadas em `src/main/resources/db/migration`, de `V1__init_schema.sql` a `V12`. O schema nunca é criado pelo Hibernate: sob Oracle o `ddl-auto` é `validate`, de modo que divergência entre entidade e tabela quebra a subida em vez de corrigir silenciosamente.

## Fluxos completos (além do CRUD)

**1. Agendamento com resolução de veterinário.** O tutor escolhe o profissional ou deixa o sistema decidir. Sem preferência, `AgendaService.veterinarioMaisTranquilo()` compara a carga do dia entre os veterinários ativos e devolve o menos ocupado. O serviço recusa horário fora do expediente, horário já tomado e — regra de negócio real — **um segundo agendamento do mesmo tutor no mesmo horário**, porque ninguém está em duas consultas ao mesmo tempo.

**2. Atendimento e pontuação.** O veterinário conclui o atendimento registrando diagnóstico, conduta e retorno; o tutor ganha pontos e pode subir de nível. Se o paciente falta, os pontos do agendamento voltam atrás. Nenhuma das duas ações é aceita antes da hora marcada: `exigirQueOHorarioJaTenhaChegado()` recusa concluir ou dar falta em atendimento que ainda não começou.

## Validações

Bean Validation nos DTOs (`@NotBlank`, `@Email`, `@CPF`, `@Size`, `@Pattern`, `@DecimalMin/Max`) e regras de negócio nos serviços, com `BusinessException` traduzida pelo `GlobalExceptionHandler` em 422 com mensagem em português. O formulário devolve o erro no campo, não uma tela de stack trace.

## Como executar localmente

```bash
git clone https://github.com/kaiky06301/animed-api
cd animed-api
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

| Endereço | O que é |
|----------|---------|
| http://localhost:8080/login | painel web |
| http://localhost:8080/swagger-ui/index.html | documentação da API |

Contas de demonstração: `doutor@animed.com.br` e `tutor@animed.com.br`, ambas com senha `animed123`.

---

# B — DevOps Tools & Cloud Computing

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
| Build da imagem | kaniko rodando como container no ACI |
| Provisionamento | Azure CLI (nenhum recurso criado pelo Portal) |

H2 existe **somente** para `mvn test` na máquina. A entrega roda em Oracle no ACI.

### Por que o build não usa `docker build`

Duas restrições reais deste ambiente:

1. **ACR Tasks é bloqueado** na subscription Azure for Students da FIAP — `az acr build` devolve `TasksOperationsNotAllowed`.
2. **Docker Desktop não está instalado** nas máquinas da equipe.

A solução é o **kaniko**: um construtor de imagens que roda como container comum. Ele sobe num ACI, clona este repositório do GitHub, executa o `Dockerfile` e dá `push` da imagem no ACR — sem daemon Docker em lugar nenhum. O build inteiro leva cerca de 70 segundos.

O `Dockerfile` continua sendo o mesmo e segue valendo com `docker build` para quem tiver Docker (veja `scripts/docker-build-push.sh`).

---

## 4. Arquitetura (recursos e fluxo)

Abra **[docs/diagrama-arquitetura.html](docs/diagrama-arquitetura.html)** no navegador — é o desenho da arquitetura com os recursos, o fluxo numerado e a legenda.

| Recurso | Papel nesta solução |
|---------|---------------------|
| GitHub | Código-fonte, `Dockerfile` e scripts. É de onde o kaniko lê o contexto do build e de onde parte o clone no vídeo. |
| Azure CLI | Cria resource group, ACR e os Container Instances. Nada pelo Portal. |
| Azure Container Registry | Guarda as duas imagens: `animed-api:1.0` e `oracle-xe:21`. |
| ACI `aci-kaniko-build` | Container temporário que compila a imagem da API e some depois. |
| ACI `aci-animed-api` | Executa a aplicação com o usuário `animed` (não-root), porta 8080. |
| ACI `aci-animed-oracle` | Executa o banco Oracle XE em container, porta 1521. |
| Flyway | Na primeira subida da API aplica as 12 migrations que criam o schema. |

Senha do banco e segredo JWT entram por **variável de ambiente protegida** (`--secure-environment-variables`): não aparecem no código nem no `az container show`.

---

## 5. Como publicar e testar (siga esta ordem no vídeo)

### 5.1 Pré-requisitos

- Git e Azure CLI (`az login`)
- Uma conta Azure com permissão para criar Resource Group, ACR e ACI
- **Não precisa de Docker** — o build acontece dentro da Azure

Opcional, só para conferir o banco por fora: SQLcl, SQL Developer ou DBeaver.

### 5.2 Clonar o repositório (começar o vídeo por aqui)

```bash
git clone https://github.com/kaiky06301/animed-api.git
cd animed-api
```

### 5.3 Entrar na Azure

```bash
az login
az account show
```

### 5.4 Definir as senhas (nunca vão para o Git)

O segredo JWT **precisa ser Base64 puro**. A aplicação faz `Decoders.BASE64.decode()`: um valor com `-` ou `_` derruba a API na subida com `Illegal base64 character`.

PowerShell (Windows):

```powershell
$env:ORACLE_PASSWORD   = 'Animed#2026Fiap'
$env:APP_USER_PASSWORD = 'Animed#2026Fiap'
$b = New-Object byte[] 48
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b)
$env:ANIMED_JWT_SECRET = [Convert]::ToBase64String($b)
```

Bash (Linux / Mac / Cloud Shell):

```bash
export ORACLE_PASSWORD='Animed#2026Fiap'
export APP_USER_PASSWORD='Animed#2026Fiap'
export ANIMED_JWT_SECRET="$(openssl rand -base64 48)"
```

### 5.5 Criar tudo com um script (Azure CLI do começo ao fim)

```powershell
.\scripts\criar-recursos-azure.ps1
```

```bash
bash scripts/criar-recursos-azure.sh
```

O script executa, em ordem: registro dos providers → resource group → ACR → import da imagem do Oracle → build da imagem da API com kaniko → ACI do banco → espera o Oracle aceitar conexão → ACI da aplicação → espera o health responder `UP`. No fim ele imprime os endereços.

### 5.6 Os mesmos passos, comando a comando

Para mostrar cada recurso nascendo na câmera, sem o script:

```bash
# 1. Resource group
az group create --name rg-animed-sprint3 --location brazilsouth

# 2. Container Registry
az acr create --resource-group rg-animed-sprint3 --name acranimed566067 \
  --sku Basic --admin-enabled true --location brazilsouth

# 3. Imagem do Oracle vai para o ACR
az acr import --name acranimed566067 \
  --source docker.io/gvenzl/oracle-xe:21-slim --image oracle-xe:21 --force

# 4. Build da imagem da API (kaniko dentro da Azure, sem Docker local)
export ACR=acranimed566067
export RG=rg-animed-sprint3
bash scripts/build-imagem-kaniko.sh

# 5. Conferir as duas imagens no registro
az acr repository list --name acranimed566067 -o table
```

```bash
# 6. Container do banco
ACR_USER=$(az acr credential show --name acranimed566067 --query username -o tsv)
ACR_PASS=$(az acr credential show --name acranimed566067 --query "passwords[0].value" -o tsv)

az container create \
  --resource-group rg-animed-sprint3 \
  --name aci-animed-oracle \
  --image acranimed566067.azurecr.io/oracle-xe:21 \
  --registry-login-server acranimed566067.azurecr.io \
  --registry-username "$ACR_USER" --registry-password "$ACR_PASS" \
  --cpu 2 --memory 4 --ports 1521 --os-type Linux \
  --dns-name-label animed-oracle-566067 --restart-policy OnFailure \
  --environment-variables ORACLE_DATABASE=CLYVOVET APP_USER=clyvo \
  --secure-environment-variables \
    ORACLE_PASSWORD="$ORACLE_PASSWORD" \
    APP_USER_PASSWORD="$APP_USER_PASSWORD"
```

Espere o Oracle abrir a porta 1521 (leva alguns minutos na primeira vez — ele cria o banco e o usuário `clyvo`). Só então:

```bash
# 7. Container da aplicação
az container create \
  --resource-group rg-animed-sprint3 \
  --name aci-animed-api \
  --image acranimed566067.azurecr.io/animed-api:1.0 \
  --registry-login-server acranimed566067.azurecr.io \
  --registry-username "$ACR_USER" --registry-password "$ACR_PASS" \
  --cpu 1 --memory 2 --ports 8080 --os-type Linux \
  --dns-name-label animed-api-566067 --restart-policy OnFailure \
  --environment-variables \
    SPRING_PROFILES_ACTIVE=oracle \
    SPRING_DATASOURCE_URL="jdbc:oracle:thin:@//animed-oracle-566067.brazilsouth.azurecontainer.io:1521/CLYVOVET" \
    SPRING_DATASOURCE_USERNAME=clyvo \
    ORACLE_SCHEMA=CLYVO \
  --secure-environment-variables \
    SPRING_DATASOURCE_PASSWORD="$APP_USER_PASSWORD" \
    ANIMED_JWT_SECRET="$ANIMED_JWT_SECRET"
```

> A cota do Azure for Students é de **6 cores** por região. Oracle (2) + API (1) + kaniko (2) cabe, mas o container do build precisa ser removido antes de subir mais coisa — os scripts já fazem isso.

### 5.7 Conferir o que subiu

```bash
az acr repository list --name acranimed566067 -o table
az container list --resource-group rg-animed-sprint3 -o table
az container show -g rg-animed-sprint3 -n aci-animed-api --query ipAddress.fqdn -o tsv
az container logs -g rg-animed-sprint3 -n aci-animed-api
```

No log da API aparece `Successfully applied 12 migrations to schema "CLYVO"` — é o Flyway criando o banco.

Endereços da aplicação publicada:

```
http://animed-api-566067.brazilsouth.azurecontainer.io:8080/login
http://animed-api-566067.brazilsouth.azurecontainer.io:8080/swagger-ui.html
http://animed-api-566067.brazilsouth.azurecontainer.io:8080/actuator/health
```

O container da aplicação **não roda como root**: o `Dockerfile` cria o usuário `animed` e declara `USER animed`. Para evidenciar:

```bash
az container exec -g rg-animed-sprint3 -n aci-animed-api --exec-command "id"
# uid=100(animed) gid=101(animed)
```

---

## 6. CRUD com evidência no banco (item 9.3 da correção)

Duas tabelas do núcleo, relacionadas entre si: **TB_TUTOR** (1) → **TB_PET** (N).

Contas criadas na subida da API:

| Perfil | E-mail | Senha |
|--------|--------|--------|
| Veterinário | doutor@animed.com.br | animed123 |
| Tutor | tutor@animed.com.br | animed123 |

### 6.1 Abrir o SELECT no banco em container

```bash
az container exec -g rg-animed-sprint3 -n aci-animed-oracle \
  --exec-command "sqlplus -s clyvo/$APP_USER_PASSWORD@//localhost:1521/CLYVOVET"
```

Ou conecte SQLcl / SQL Developer / DBeaver em:

```
host    animed-oracle-566067.brazilsouth.azurecontainer.io
porta   1521
service CLYVOVET
usuário clyvo
```

Os dois SELECTs usados o tempo todo:

```sql
SELECT ID_TUTOR, NOME, EMAIL, TELEFONE FROM TB_TUTOR ORDER BY ID_TUTOR;
SELECT ID_PET, NOME, ESPECIE, RACA, PESO_KG, ID_TUTOR FROM TB_PET ORDER BY ID_PET;
```

### 6.2 Sequência a executar (sem cortes no vídeo)

Deixe o Swagger (ou o painel em `/login`) de um lado e o SELECT do outro. A cada operação, rode o SELECT de novo.

| # | Operação | Onde | O que mostrar no banco |
|---|----------|------|------------------------|
| 1 | `POST /api/auth/login` com o doutor | Swagger | — (pega o token) |
| 2 | **Consulta**: `GET /api/tutores` e `GET /api/pets` | Swagger | SELECT nas duas tabelas: as linhas do seed |
| 3 | **Inclusão**: `POST /api/tutores` | Swagger | SELECT em TB_TUTOR: a linha nova apareceu |
| 4 | **Inclusão**: `POST /api/pets` com `idTutor` do tutor criado | Swagger | SELECT em TB_PET: linha nova com a FK apontando para o tutor |
| 5 | **Alteração**: `PUT /api/tutores/{id}` mudando telefone | Swagger | SELECT em TB_TUTOR: telefone mudou na mesma linha |
| 6 | **Alteração**: `PUT /api/pets/{id}` mudando o peso | Swagger | SELECT em TB_PET: peso mudou |
| 7 | **Exclusão**: `DELETE /api/pets/{id}` | Swagger | SELECT em TB_PET: a linha sumiu |
| 8 | **Exclusão**: `DELETE /api/tutores/{id}` | Swagger | SELECT em TB_TUTOR: a linha sumiu |

Corpos prontos para copiar:

```json
POST /api/tutores
{
  "nome": "Marcos Ferreira",
  "email": "marcos.ferreira@animed.com.br",
  "cpf": "318.444.190-07",
  "telefone": "(11) 98822-4410"
}
```

```json
POST /api/pets
{
  "nome": "Bidu",
  "especie": "CACHORRO",
  "sexo": "MACHO",
  "raca": "Beagle",
  "dataNascimento": "2021-08-09",
  "pesoKg": 12.4,
  "castrado": true,
  "observacoesSaude": "Alergia a frango",
  "idTutor": 5
}
```

O seed do Flyway já entrega 4 tutores e 4 pets com conteúdo real (Marina/Thor, Carlos/Rex, …), o que cobre o requisito das **2+ linhas significativas** nas duas tabelas. As linhas do CRUD acima são criadas e apagadas na frente da câmera.

---

## 7. Scripts entregues

| Arquivo | Para quê |
|---------|----------|
| `Dockerfile` | Imagem da API em dois estágios; usuário `animed`, não-root |
| `docker-compose.yml` | Mesmo desenho (app + Oracle em container) na máquina |
| `scripts/criar-recursos-azure.ps1` | Cria **tudo** na Azure via CLI — Windows |
| `scripts/criar-recursos-azure.sh` | Idem, em bash / Cloud Shell |
| `scripts/build-imagem-kaniko.sh` | Build e push da imagem dentro da Azure, sem Docker |
| `scripts/docker-build-push.sh` | Alternativa com `docker build` para quem tem Docker |
| `scripts/remover-recursos-azure.sh` | Apaga o resource group inteiro |
| `scripts/dados-demo.sh` | Recria o cenário de demonstração pelos endpoints |
| `script_bd.sql` | DDL comentado das tabelas + carga mínima |
| `src/main/resources/db/migration/` | As 12 migrations que o Flyway aplica na nuvem |

### Limpar tudo depois da gravação

```bash
bash scripts/remover-recursos-azure.sh
# ou
az group delete --name rg-animed-sprint3 --yes --no-wait
```

---

## 8. Rotas usadas na correção

| Método | Rota | Uso no vídeo |
|--------|------|----------------|
| POST | `/api/auth/login` | Pegar o JWT |
| GET | `/api/tutores` | Consultar tutores |
| POST | `/api/tutores` | Inserir tutor |
| PUT | `/api/tutores/{id}` | Atualizar tutor |
| DELETE | `/api/tutores/{id}` | Excluir tutor |
| GET | `/api/pets` | Consultar pets |
| POST | `/api/pets` | Inserir pet (FK do tutor) |
| PUT | `/api/pets/{id}` | Atualizar pet |
| DELETE | `/api/pets/{id}` | Excluir pet |

Painel web: `/login`. Swagger: `/swagger-ui.html`. Health: `/actuator/health`.

---

## 9. Equipe

| Nome | RM |
|------|------|
| Erick Bernardes Bradaschia | 565733 |
| Gabriel Santos Claudino | 564054 |
| Kaiky de Oliveira Silva | 566067 |
| Lucas Fortes de Lima | 559523 |
| Jonathan Moreira Gomes | 565060 |

Turma 2TDS — FIAP.

O PDF da entrega tem **somente** nomes, RMs, GitHub e YouTube. O resto está neste README.
