# 🐾 Clyvo VET API

> **API REST de cuidado contínuo e gamificação para pets** - Challenge FIAP 2026

Sistema backend que materializa a proposta da **Clyvo VET**: transformar a jornada de saúde do pet de um modelo *episódico e reativo* em uma experiência **contínua, preventiva e gamificada**, conectando tutores, clínicas e pet shops parceiros num ecossistema único.

---

## 📋 Sumário

- [Visão Geral](#-visão-geral)
- [Diferencial — Sistema de Gamificação](#-diferencial--sistema-de-gamificação)
- [Stack Tecnológica](#️-stack-tecnológica)
- [Arquitetura](#-arquitetura)
- [Modelo de Domínio](#-modelo-de-domínio)
- [Como Executar](#-como-executar)
- [Documentação das Rotas](#-documentação-das-rotas)
- [Equipe](#-equipe)

---

## 🎯 Visão Geral

A **Clyvo VET API** resolve um problema crítico do mercado pet brasileiro: a **descontinuidade do cuidado**. Hoje, tutores só interagem com o ecossistema veterinário em momentos pontuais (vacinas, emergências), o que gera:

- ❌ Pior cuidado preventivo
- ❌ Menor LTV para clínicas
- ❌ Baixa fidelização
- ❌ Histórico clínico fragmentado

### Nossa Solução

Uma **plataforma gamificada** onde cada ação de cuidado vira pontos, descontos e benefícios — alinhando interesses de tutores, clínicas e pet shops.

### Benefícios para o Negócio

| Stakeholder | Benefício |
|-------------|-----------|
| **Tutor** | Cuidado preventivo recompensado, descontos progressivos, organização da saúde do pet |
| **Pet** | Maior frequência de check-ups, vacinas em dia, qualidade de vida |
| **Clínicas** | Recorrência, fidelização, maior LTV, base de dados longitudinal |
| **Pet Shops** | Mais vendas via tráfego qualificado, modelo de baixo risco (taxa+comissão) |
| **Clyvo** | Comissão sobre vendas + assinatura mensal de parceiros + plano premium do usuário |

---

## 🎮 Diferencial — Sistema de Gamificação

### Como o tutor ganha pontos:

| Ação | Pontos |
|------|--------|
| Cadastrar pet | +50 |
| Completar perfil do pet | +100 |
| Registrar vacinação | +30 |
| Registrar consulta | +40 |
| Check-up realizado | +60 |
| Atualizar peso | +10 |
| Compra em parceiro | 1 ponto / R$10 |

### Sistema de Níveis

| Nível | Pontos | Desconto |
|-------|--------|----------|
| 🥉 **Básico** | 0–99 | 0% |
| 🥈 **Cuidador** | 100–499 | 5% |
| 🥇 **Tutor Premium** | 500+ | 15% |

### Como funciona o desconto na prática:

```
Produto: Ração Premium R$ 150,00
Tutor BÁSICO   → paga R$ 150,00 (0% desc)
Tutor CUIDADOR → paga R$ 142,50 (5% desc) → Clyvo ganha 5% comissão = R$ 7,13
Tutor PREMIUM  → paga R$ 127,50 (15% desc) → Clyvo ganha 5% comissão = R$ 6,38
```

---

## 🛠️ Stack Tecnológica

- **Java 17**
- **Spring Boot 3.3.4**
  - Spring Web (REST)
  - Spring Data JPA
  - Spring Validation (Bean Validation)
  - Spring Cache (Caffeine)
  - Spring HATEOAS (Nível 3 de Maturidade Richardson)
- **Oracle Database** (produção FIAP) + **H2** (dev local)
- **Flyway** (migrations versionadas)
- **Lombok**
- **SpringDoc OpenAPI 3** (Swagger UI)
- **Maven**

---

## 🏗️ Arquitetura

```
┌─────────────────┐
│   Controller    │  ← REST endpoints, Swagger, HATEOAS
└────────┬────────┘
         │
┌────────▼────────┐
│     Service     │  ← Regra de negócio, gamificação, cache
└────────┬────────┘
         │
┌────────▼────────┐
│   Repository    │  ← Spring Data JPA + JPQL
└────────┬────────┘
         │
┌────────▼────────┐
│   Entity (JPA)  │  ← Modelo de domínio mapeado
└────────┬────────┘
         │
┌────────▼────────┐
│  Oracle / H2    │
└─────────────────┘
```

### Padrões aplicados

- **DTO Pattern** — separação entre modelo de domínio e API
- **Builder Pattern** (via Lombok) — construção fluente de entidades
- **Repository Pattern** — abstração de persistência
- **Dependency Injection** — via construtor (`@RequiredArgsConstructor`)
- **Global Exception Handler** — padronização de erros

---

## 📊 Modelo de Domínio

### Entidades principais

| Entidade | Descrição |
|----------|-----------|
| `Tutor` | Responsável pelo pet, possui pontos e nível de gamificação |
| `Pet` | Animal cadastrado (Cachorro, Gato, etc.) com histórico clínico |
| `Vacina` | Registro de vacinação aplicada no pet |
| `Consulta` | Agendamento/registro de consulta veterinária |
| `PetShop` | Parceiro B2B (taxa mensal + comissão por venda) |
| `TransacaoParceiro` | Compra realizada por tutor em pet shop |
| `HistoricoPontuacao` | Auditoria de todas as ações que renderam pontos |

### Relacionamentos

```
Tutor (1) ──── (N) Pet
Tutor (1) ──── (N) HistoricoPontuacao
Tutor (1) ──── (N) TransacaoParceiro
Pet   (1) ──── (N) Vacina
Pet   (1) ──── (N) Consulta
PetShop (1) ── (N) TransacaoParceiro
PetShop (1) ── (N) Consulta
```

> 📎 Diagrama de classes detalhado em `docs/DIAGRAMA_CLASSES.md`
> 📎 Diagrama Entidade-Relacionamento em `docs/DER.png` (gerado pela equipe de Database no Oracle Data Modeler)

---

## 🚀 Como Executar

### Pré-requisitos

- Java 17+
- Maven 3.8+
- Oracle XE rodando localmente **OU** banco da FIAP (`oracle.fiap.com.br:1521:ORCL`)

### Passo 1 — Clonar o repositório

```bash
git clone https://github.com/<usuario>/clyvo-vet-api.git
cd clyvo-vet-api
```

### Passo 2 — Configurar credenciais (Oracle FIAP)

Edite `src/main/resources/application-oracle.properties`:

```properties
spring.datasource.username=RMxxxxxx       # seu RM
spring.datasource.password=xxxxxx         # geralmente o RM sem letras
spring.jpa.properties.hibernate.default_schema=RMxxxxxx
```

### Passo 3 — Rodar com Oracle (default)

```bash
./mvnw spring-boot:run
```

### Passo 3 (alternativa) — Rodar com H2 (testes locais rápidos)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

H2 Console: http://localhost:8080/h2-console

### Passo 4 — Acessar a documentação Swagger

🔗 **http://localhost:8080/swagger-ui.html**

---

## 📡 Documentação das Rotas

### 🧑 Tutores (`/api/tutores`)

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/tutores` | Criar tutor |
| `GET` | `/api/tutores/{id}` | Buscar por ID (com HATEOAS) |
| `GET` | `/api/tutores` | Listar (paginado, ordenável) |
| `GET` | `/api/tutores/buscar?nome=` | Buscar por nome (LIKE) |
| `GET` | `/api/tutores/por-nivel/{nivel}` | Filtrar por nível de gamificação |
| `GET` | `/api/tutores/ranking` | Ranking de pontuação |
| `PUT` | `/api/tutores/{id}` | Atualizar |
| `DELETE` | `/api/tutores/{id}` | Deletar |

### 🐶 Pets (`/api/pets`)

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/pets` | Cadastrar pet (gera pontos automáticos) |
| `GET` | `/api/pets/{id}` | Buscar por ID |
| `GET` | `/api/pets` | Listar |
| `GET` | `/api/pets/por-tutor/{idTutor}` | Pets de um tutor |
| `GET` | `/api/pets/por-especie/{especie}` | Filtrar por espécie |
| `GET` | `/api/pets/buscar?nome=` | Buscar por nome |
| `PUT` | `/api/pets/{id}` | Atualizar |
| `DELETE` | `/api/pets/{id}` | Deletar |

### 💉 Vacinas (`/api/vacinas`)

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/vacinas` | Registrar vacinação (+30 pontos) |
| `GET` | `/api/vacinas/{id}` | Buscar por ID |
| `GET` | `/api/vacinas` | Listar |
| `GET` | `/api/vacinas/por-pet/{idPet}` | Vacinas de um pet |
| `GET` | `/api/vacinas/por-tutor/{idTutor}` | Vacinas de todos pets de um tutor |
| `PUT` | `/api/vacinas/{id}` | Atualizar |
| `DELETE` | `/api/vacinas/{id}` | Deletar |

### 🩺 Consultas (`/api/consultas`)

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/consultas` | Agendar consulta |
| `GET` | `/api/consultas/{id}` | Buscar por ID |
| `GET` | `/api/consultas` | Listar |
| `GET` | `/api/consultas/por-pet/{idPet}` | Consultas de um pet |
| `GET` | `/api/consultas/por-tutor/{idTutor}` | Consultas de um tutor |
| `GET` | `/api/consultas/por-status/{status}` | Filtrar por status |
| `PUT` | `/api/consultas/{id}` | Atualizar (REALIZADA dispara +60 pontos) |
| `DELETE` | `/api/consultas/{id}` | Deletar |

### 🏪 Pet Shops (`/api/petshops`)

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/petshops` | Cadastrar parceiro |
| `GET` | `/api/petshops/{id}` | Buscar por ID |
| `GET` | `/api/petshops` | Listar |
| `GET` | `/api/petshops/ativos` | Apenas ativos |
| `GET` | `/api/petshops/por-localizacao?cidade=&uf=` | Por cidade/UF |
| `GET` | `/api/petshops/buscar?nome=` | Por nome fantasia |
| `PUT` | `/api/petshops/{id}` | Atualizar |
| `DELETE` | `/api/petshops/{id}` | Deletar |

### 💳 Transações (`/api/transacoes`)

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/transacoes` | Registrar compra (aplica desconto auto) |
| `GET` | `/api/transacoes/{id}` | Buscar por ID |
| `GET` | `/api/transacoes` | Listar |
| `GET` | `/api/transacoes/por-tutor/{idTutor}` | Transações de um tutor |
| `GET` | `/api/transacoes/por-petshop/{idPetShop}` | Transações de um pet shop |
| `GET` | `/api/transacoes/relatorios/comissao-petshop/{id}` | Total de comissão |
| `GET` | `/api/transacoes/relatorios/gasto-tutor/{id}` | Total gasto |
| `DELETE` | `/api/transacoes/{id}` | Estornar |

### 📜 Histórico (`/api/historico-pontuacao`)

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/api/historico-pontuacao` | Listar todo histórico |
| `GET` | `/api/historico-pontuacao/por-tutor/{id}` | Por tutor |
| `GET` | `/api/historico-pontuacao/por-tipo/{tipo}` | Por tipo de ação |

---

## ✅ Requisitos Atendidos (Rubrica FIAP)

| Requisito | Status | Onde encontrar |
|-----------|--------|----------------|
| Spring Boot + JPA | ✅ | `pom.xml`, todas as entidades |
| Entidades com relacionamentos | ✅ | `entity/` (7 entidades) |
| POO + Coesão + Desacoplamento | ✅ | Service/Controller/Repository separados |
| API RESTful (Richardson nível 3) | ✅ | `TutorController` com HATEOAS |
| Padrões de projeto | ✅ | Builder, DTO, Repository, Singleton |
| JPQL + Query Methods | ✅ | `TutorRepository`, `VacinaRepository`, etc. |
| Bean Validation | ✅ | Todos os DTOs `Request` |
| Paginação | ✅ | Todos endpoints de listagem |
| Ordenação | ✅ | `Pageable` com `sort=campo,asc\|desc` |
| Busca com parâmetros | ✅ | `/buscar?nome=`, `/por-tutor/{id}`, etc. |
| Cache | ✅ | `@Cacheable` em TutorService, PetShopService |
| Tratamento de erros | ✅ | `GlobalExceptionHandler` |
| DTOs | ✅ | `dto/` (8 DTOs) |
| Swagger | ✅ | http://localhost:8080/swagger-ui.html |
| Postman Collection | ✅ | `postman/Clyvo-VET-API.postman_collection.json` |

---

## 👥 Equipe

| Nome | RM |
|------|------|
| Erick Bernardes Bradaschia | 565733 |
| Gabriel Santos Claudino | 564054 |
| **Kaiky de Oliveira Silva** *(líder)* | 566067 |
| Lucas Fortes de Lima | 559523 |
| Jonathan Moreira Gomes | 565060 |

**Turma:** 2TDS Fevereiro/2026 - FIAP

---

## 📄 Licença

Projeto acadêmico desenvolvido para o Challenge FIAP 2026 em parceria com a Clyvo VET.
