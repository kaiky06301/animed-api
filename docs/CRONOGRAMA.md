# 📅 Cronograma de Desenvolvimento - Squad Clyvo VET

**Challenge FIAP 2026 — Entrega: 24/05/2025**
**Turma:** 2TDS Fevereiro/2026

---

## 👥 Equipe e Responsabilidades

| Integrante | RM | Disciplina(s) | Status |
|------------|------|----------------|--------|
| Erick Bernardes Bradaschia | 565733 | *(a definir)* | 🟡 |
| Gabriel Santos Claudino | 564054 | *(a definir)* | 🟡 |
| **Kaiky de Oliveira Silva** *(líder)* | 566067 | *(a definir)* | 🟢 |
| Lucas Fortes de Lima | 559523 | *(a definir)* | 🟡 |
| Jonathan Moreira Gomes | 565060 | *(a definir)* | 🟡 |

---

## 🗓️ Cronograma Java Advanced

### Sprint 1 + 2 (entrega única em 24/05)

| Fase | Atividade | Responsável | Início | Fim | Status |
|------|-----------|-------------|--------|-----|--------|
| 1 | Modelagem inicial e definição de entidades | Java + DB | 16/04 | 20/04 | ✅ |
| 2 | Setup do projeto Spring Boot (pom, properties, estrutura) | Java | 20/04 | 22/04 | ✅ |
| 3 | Criação de enums e entidades JPA | Java | 22/04 | 26/04 | ✅ |
| 4 | DTOs com Bean Validation | Java | 26/04 | 29/04 | ✅ |
| 5 | Repositories (JpaRepository + JPQL) | Java | 29/04 | 02/05 | ✅ |
| 6 | Services (regra de negócio + gamificação) | Java | 02/05 | 08/05 | ✅ |
| 7 | Controllers REST + Swagger | Java | 08/05 | 13/05 | ✅ |
| 8 | Exception Handler global | Java | 13/05 | 14/05 | ✅ |
| 9 | HATEOAS (Richardson nível 3) | Java | 14/05 | 15/05 | ✅ |
| 10 | Cache com Caffeine | Java | 15/05 | 16/05 | ✅ |
| 11 | Migrations Flyway + dados iniciais | Java + DB | 16/05 | 18/05 | ✅ |
| 12 | Postman collection + testes manuais | Java | 18/05 | 20/05 | ✅ |
| 13 | README + documentação técnica | Java | 20/05 | 22/05 | ✅ |
| 14 | Testes finais e ajustes | Squad | 22/05 | 23/05 | 🟡 |
| 15 | **ENTREGA FIAP** | Líder (Kaiky) | **24/05** | **24/05** | 🔴 |

---

## 🔗 Dependências entre disciplinas

```
[Java] ──→ entrega entidades → [Database] modela DER no Oracle Data Modeler
   │
   ├──→ API rodando → [DevOps] containeriza + sobe na Azure
   │
   └──→ documentação OpenAPI → [Mobile] consome (ou mocka)

[Compliance/QA] ─── trabalha em paralelo (independente de código)
[IoT/IA]        ─── trabalha em paralelo (independente de código)
```

---

## ✅ Entregáveis Java (checklist)

- [x] Spring Boot + JPA configurado
- [x] 7 Entidades com relacionamentos JPA
- [x] DTOs com Bean Validation
- [x] CRUD REST completo para todas as entidades
- [x] Paginação em todos os listings
- [x] Ordenação configurável
- [x] Busca por parâmetros (`@RequestParam`)
- [x] Cache com `@Cacheable` (Caffeine)
- [x] Tratamento global de exceções
- [x] Swagger/OpenAPI documentado
- [x] JPQL + Spring Data Query Methods
- [x] Padrões de projeto aplicados (Builder, DTO, Repository)
- [x] Richardson Maturity Model nível 3 (HATEOAS)
- [x] Migrations Flyway (Oracle)
- [x] README completo
- [x] Postman collection exportada
- [x] Diagrama de Classes
- [x] Repositório público no GitHub
