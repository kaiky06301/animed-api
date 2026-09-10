# 🐾 Animed — Memória do Projeto (Challenge FIAP 2026)

> Este arquivo é a **memória permanente do projeto**. Toda vez que alguém do squad abrir o Claude Code
> nesta pasta, este contexto é carregado. Ele descreve o **problema**, a **solução**, a **rubrica de
> avaliação** e as **regras de ouro**. Leia as seções [Regras de Ouro](#️-regras-de-ouro-do-projeto) e
> [Antes de Qualquer Mudança](#-antes-de-qualquer-mudança-no-código) antes de tocar em código.

- **Produto / marca:** Animed
- **Empresa parceira (cliente do desafio):** Clyvo VET
- **Challenge:** FIAP 2026 — 2TDS Fevereiro
- **Entrega:** 24/05/2025
- **Squad:** 5 integrantes
- **Repositório:** https://github.com/kaiky06301/animed-api

> ⚠️ **Branding x código:** o produto chama-se **Animed**, mas o rebrand foi só de superfície.
> O pacote Java continua `br.com.fiap.clyvovet`, a classe principal `ClyvoVetApplication`, o
> `artifactId` `clyvo-vet-api` e os identificadores Oracle (`CLYVOVET`, `CLYVO`, usuário `clyvo`,
> coluna `COMISSAO_CLYVO`) — **tudo isso é funcional e NÃO deve ser alterado** (ver Regras de Ouro).

---

## 📍 Contexto do Challenge

A **Clyvo VET** (empresa parceira) propôs o desafio. Nossa entrega é o **Animed**, posicionado como
*"Animed — solução para o Challenge da Clyvo VET"*.

---

## ❌ Problema (definido pela Clyvo VET)

A jornada de saúde do pet é **FRAGMENTADA, EPISÓDICA e REATIVA**. O tutor só interage com o
ecossistema veterinário em momentos pontuais:

- Sintomas agudos e vacinas
- Exames e retornos solicitados
- Pós-operatório e doenças crônicas
- Emergências e check-ups esporádicos

### Dores geradas

| Para quem | Dores |
|-----------|-------|
| **Tutor** | Esquecimento, insegurança, dificuldade de continuidade, falta de orientação, baixa previsibilidade |
| **Pet** | Pior cuidado preventivo, pior qualidade de vida, agravamento evitável, baixa adesão clínica |
| **Clínicas** | Perda de recorrência, baixa fidelização, abandono de tratamentos, menor LTV, subutilização da base |

**Objetivo da Clyvo:** transformar a jornada de **EPISÓDICA → CONTÍNUA, PREVENTIVA, INTELIGENTE e INTEGRADA**.

---

## ✅ Solução (Animed — nossa marca)

- **Nome do produto:** ANIMED
- **Posicionamento:** *"Sistema de cuidado contínuo para pets via gamificação"*

**Diferencial principal:** transformar o cuidado com o pet em uma **experiência engajante, recompensada
e contínua**, através de um sistema de gamificação que conecta **tutores, clínicas e pet shops parceiros**.

### Como funciona — Sistema de Pontos

**📝 Onboarding (cadastro)**
- Inserir raça → +pontos
- Inserir idade → +pontos
- Inserir peso → +pontos
- Inserir histórico de saúde → +pontos
- Completar perfil → bônus

**🔄 Atualizações contínuas**
- Atualizar peso regularmente
- Registrar vacinação
- Registrar medicação
- Registrar consultas

**🩺 Ações de cuidado**
- Check-ups realizados
- Vacinas em dia
- Agendamento de consultas
- Uso do app

**🛍️ Consumo em parceiros**
- Compras em pet shops parceiros
- Check-in em estabelecimentos

### 🏆 Sistema de Níveis

| Nível | Faixa de pontos | Desconto |
|-------|-----------------|----------|
| 🥉 Nível 1 — Básico | 0–99 pts | 0% |
| 🥈 Nível 2 — Cuidador | 100–499 pts | 5% |
| 🥇 Nível 3 — Tutor Premium | 500+ pts | 15% |

---

## 💰 Monetização

### B2C (Usuário)

| Plano | Inclui |
|-------|--------|
| 🆓 **Gratuito** | Cadastro do pet, lembretes básicos, pontuação inicial |
| 💼 **Intermediário** | Relatórios personalizados, gamificação completa, alertas inteligentes |
| 👑 **Premium** | Telemedicina (futuro), IA personalizada, suporte prioritário |

### B2B (Pet Shops) — Modelo Híbrido

1. **Taxa Base (baixo risco):** pet shop paga valor fixo pequeno para estar na plataforma.
2. **Comissão por Performance:** % sobre vendas geradas pelo app.

### Exemplo prático

Produto: **Ração R$ 100**

| Cliente | Paga | Comissão Animed (5%) |
|---------|------|----------------------|
| Sem pontos | R$ 100 | — |
| Cuidador (5% desc) | R$ 95 | R$ 4,75 |
| Premium (15% desc) | R$ 85 | R$ 4,25 |

**Por que vale a pena pro pet shop:** aumenta volume de vendas, ganha novos clientes, fideliza clientes
existentes e vende com recorrência.

---

## 🧠 Inteligência + Comunidade

- Análise de comportamento do pet
- Recomendações personalizadas
- Alertas preditivos
- Integração com IoT (coleiras inteligentes — futuro)
- Comunidade: donos compartilham experiências, fotos e dicas por raça

---

## 👥 Squad Animed

| Integrante | RM | Papel |
|------------|------|-------|
| **Kaiky de Oliveira Silva** | 566067 | **Líder** + Java Backend |
| Erick Bernardes Bradaschia | 565733 | Database (Oracle) |
| Gabriel Santos Claudino | 564054 | — |
| Lucas Fortes de Lima | 559523 | — |
| Jonathan Moreira Gomes | 565060 | — |

---

## 🎯 Disciplinas do Challenge

| # | Disciplina | Stack | Status |
|---|------------|-------|--------|
| 1 | **Java Advanced** | Spring Boot + JPA | ✅ **ENTREGUE** |
| 2 | .NET Advanced | ASP.NET Core + EF Core | — |
| 3 | Mastering Database | Oracle Data Modeler + PL/SQL | — |
| 4 | DevOps | Docker + Azure | — |
| 5 | Mobile | React Native + Expo | — |
| 6 | IoT/IA | Sensores ou visão computacional | — |
| 7 | Compliance/QA | Pitch + TOGAF + Riscos + Métricas | — |

---

## 📋 Rubrica Java Advanced (FIAP — 100 pts)

> ⚠️ **TODOS os requisitos abaixo já estão implementados.** Se for mexer em código, **NÃO REMOVA**
> nenhum deles. Antes de qualquer alteração que possa impactar algum item, **avise o Kaiky**.

| Pontos | Requisito | Onde / Observação | Status |
|-------:|-----------|-------------------|:------:|
| até 5 | Cronograma de desenvolvimento | `docs/CRONOGRAMA.md` | ✅ |
| até 10 | Diagrama de Classes + DER coerentes | `docs/DIAGRAMA_CLASSES.md` (Mermaid). As entidades JPA devem refletir o `V1__init_schema.sql` | ✅ |
| até 40 | Implementação das classes de Entidade | `src/main/java/br/com/fiap/clyvovet/entity/` — 7 entidades com `@Entity`, `@Table`, `@Id`, relacionamentos (`@OneToMany`, `@ManyToOne`) e `@ForeignKey` nomeada | ✅ |
| até 15 | REST + Richardson Maturity Model | Controllers com `ResponseEntity` e status corretos (200/201/204/400/404/422); `TutorController` com HATEOAS (nível 3 — `EntityModel` + links) | ✅ |
| até 10 | Gestão de Configuração (artefatos no GitHub) | Repo público | ✅ |
| até 10 | Link público do GitHub | https://github.com/kaiky06301/animed-api | ✅ |
| até 10 | Testes documentados (Postman/Insomnia exportado) | `postman/Animed-API.postman_collection.json` | ✅ |

### As 7 Entidades

`Tutor`, `Pet`, `Vacina`, `Consulta`, `PetShop`, `TransacaoParceiro`, `HistoricoPontuacao`

---

## 🔧 Requisitos Técnicos Obrigatórios (citados pela rubrica)

> Todos **JÁ implementados — NÃO REMOVER**.

| Requisito | Implementação |
|-----------|---------------|
| ✅ **Bean Validation** | `@NotBlank`, `@Email`, `@CPF`, `@Size`, `@Pattern`, `@DecimalMin/Max` nos DTOs (`src/main/java/br/com/fiap/clyvovet/dto/`) |
| ✅ **Paginação** | `Pageable` em todos os listings; `@PageableDefault(size = 10, sort = "campo")` |
| ✅ **Ordenação** | Via parâmetro `sort` do `Pageable` (ex.: `?sort=nome,asc`) |
| ✅ **Busca com parâmetros** | `@RequestParam` em `/buscar?nome=`; `@PathVariable` em `/por-tutor/{id}`, `/por-especie/{especie}` |
| ✅ **Cache** | `@Cacheable` em `TutorService` e `PetShopService`; `@CacheEvict` em mudanças de estado; **Caffeine** (config em `application.properties`, `spring.cache.caffeine.spec`) |
| ✅ **Tratamento de erros** | `GlobalExceptionHandler` com `@RestControllerAdvice`; exceptions custom `ResourceNotFoundException`, `BusinessException` (`src/main/java/br/com/fiap/clyvovet/exception/`) |
| ✅ **DTOs** | `records` (Java 17+); padrão `ClasseDTO.Request` (input) + `ClasseDTO.Response` (output); separação domínio ↔ API |
| ✅ **Swagger / OpenAPI** | SpringDoc OpenAPI 3; `@Tag`, `@Operation`, `@ApiResponse(s)`; http://localhost:8080/swagger-ui.html — **33 endpoints** |
| ✅ **JPQL / Query Methods** | Query Methods (`findByEmail`, `findByNomeContainingIgnoreCase`, …) e JPQL com `@Query` (ranking, totais, joins) — `src/main/java/br/com/fiap/clyvovet/repository/` |
| ✅ **POO + Coesão + Desacoplamento** | Camadas Controller → Service → Repository → Entity; DI por construtor (`@RequiredArgsConstructor`); Builder (Lombok `@Builder`) |
| ✅ **Padrões de projeto** | Builder (Lombok), DTO, Repository, Dependency Injection, Singleton (Spring Beans) |

---

## 🗄️ Persistência

- **Banco principal:** Oracle FIAP — perfil `oracle` (**default**)
- **Banco de testes locais:** H2 em `MODE=Oracle` — perfil `h2`

### Migrations Flyway

- `V1__init_schema.sql` → schema completo (7 tabelas, FKs, constraints, índices)
- `V2__seed_data.sql` → dados iniciais (tutores, pets, vacinas, etc.)

### ⚠️ Tipos numéricos (importante)

- Sempre **`BigDecimal`** para campos com `scale > 0` (peso, valor, comissão, taxa).
- **NUNCA `Double` com `@Column(scale = X)`** — o Hibernate 6 rejeita (`scale has no meaning for floating point types`).
- `@DecimalMin/Max` funcionam com `BigDecimal` nativamente.

### Configuração por perfil

| Perfil | `ddl-auto` | Motivo |
|--------|------------|--------|
| `h2` (dev) | `none` | Flyway cria o schema; o Hibernate não valida. Sob H2, `NUMBER` (Oracle) vira `NUMERIC` e o `validate` acusa falso mismatch com `BIGINT`. |
| `oracle` (prod) | `validate` | Validação ativa contra o schema real. |

---

## 🚨 Penalidades Possíveis (NÃO COMETER)

- ❌ Remover qualquer dos requisitos técnicos acima → **perda de pontos diretos**.
- ❌ Quebrar a relação **DER ↔ Diagrama de Classes ↔ Entidades JPA** → perda em coerência.
- ❌ Remover **HATEOAS** → cai de nível 3 para nível 2 do Richardson Maturity Model.
- ❌ Tornar o repo **privado** → professor não consegue avaliar (**zero**).
- ❌ Mexer no pacote **`br.com.fiap.clyvovet`** → quebra o projeto inteiro.

---

## ⚠️ Regras de Ouro do Projeto

1. **NÃO** mude pacotes Java (`br.com.fiap.clyvovet`) — são identificadores funcionais.
2. **NÃO** mude credenciais Oracle (`CLYVOVET`, `CLYVO`, usuário `clyvo` no `docker-compose`).
3. **NÃO** mude a coluna `COMISSAO_CLYVO` nem o campo `comissaoClyvo`.
4. **SEMPRE** que falar de "marca/produto", use **"Animed"**.
5. **SEMPRE** que falar de "empresa parceira/cliente", use **"Clyvo VET"**.
6. Pitch e documentação: posicionar como **"Animed — solução para o Challenge da Clyvo VET"**.

---

## ✅ Antes de Qualquer Mudança no Código

1. **Confirme com o Kaiky** o que vai mudar.
2. **Liste** quais requisitos da rubrica podem ser impactados.
3. Rode **`./mvnw test`** após qualquer alteração.
4. Faça **commit semântico** (`chore` / `feat` / `fix` / `docs` / `test` / `refactor`).
5. **NÃO** faça force push — mantenha o histórico íntegro.
