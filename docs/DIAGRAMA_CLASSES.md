# 📐 Diagrama de Classes - Clyvo VET

## Diagrama UML (Mermaid)

```mermaid
classDiagram
    class Tutor {
        -Long id
        -String nome
        -String email
        -String cpf
        -String telefone
        -Integer pontosTotais
        -NivelGamificacao nivel
        -PlanoAssinatura plano
        -LocalDateTime dataCadastro
        -Boolean ativo
        +adicionarPontos(int) void
    }

    class Pet {
        -Long id
        -String nome
        -Especie especie
        -String raca
        -LocalDate dataNascimento
        -Double pesoKg
        -Boolean castrado
        -String observacoesSaude
    }

    class Vacina {
        -Long id
        -String nomeVacina
        -LocalDate dataAplicacao
        -LocalDate dataProximaDose
        -String veterinarioResponsavel
        -String clinica
        -String lote
    }

    class Consulta {
        -Long id
        -LocalDateTime dataHora
        -String motivo
        -String diagnostico
        -String prescricao
        -BigDecimal valor
        -StatusConsulta status
        -String veterinario
    }

    class PetShop {
        -Long id
        -String razaoSocial
        -String nomeFantasia
        -String cnpj
        -String cidade
        -String uf
        -BigDecimal taxaMensal
        -BigDecimal comissaoPercentual
        -Boolean ativo
    }

    class TransacaoParceiro {
        -Long id
        -LocalDateTime dataHora
        -BigDecimal valorBruto
        -BigDecimal descontoAplicado
        -BigDecimal valorFinal
        -BigDecimal comissaoClyvo
        -Integer pontosGerados
        -String descricaoProduto
        +calcularValores() void
    }

    class HistoricoPontuacao {
        -Long id
        -TipoAcaoPontuacao tipoAcao
        -Integer pontosGanhos
        -String descricao
        -LocalDateTime dataHora
    }

    class NivelGamificacao {
        <<enum>>
        BASICO
        CUIDADOR
        TUTOR_PREMIUM
        +fromPontos(int) NivelGamificacao
    }

    class PlanoAssinatura {
        <<enum>>
        GRATUITO
        INTERMEDIARIO
        PREMIUM
    }

    class Especie {
        <<enum>>
        CACHORRO
        GATO
        AVE
        ROEDOR
        REPTIL
        OUTRO
    }

    class StatusConsulta {
        <<enum>>
        AGENDADA
        REALIZADA
        CANCELADA
        NAO_COMPARECEU
    }

    class TipoAcaoPontuacao {
        <<enum>>
        CADASTRO_PET
        PERFIL_COMPLETO
        REGISTRO_VACINA
        REGISTRO_CONSULTA
        REGISTRO_MEDICACAO
        ATUALIZACAO_PESO
        CHECKUP_REALIZADO
        COMPRA_PARCEIRO
        CHECKIN_ESTABELECIMENTO
        MISSAO_CONCLUIDA
    }

    Tutor "1" --> "*" Pet : possui
    Tutor "1" --> "*" HistoricoPontuacao : acumula
    Tutor "1" --> "*" TransacaoParceiro : realiza
    Tutor --> NivelGamificacao : tem
    Tutor --> PlanoAssinatura : assinou

    Pet "1" --> "*" Vacina : recebe
    Pet "1" --> "*" Consulta : tem
    Pet --> Especie : é

    Consulta --> StatusConsulta : tem
    Consulta "*" --> "0..1" PetShop : pode ocorrer em

    PetShop "1" --> "*" TransacaoParceiro : recebe
    HistoricoPontuacao --> TipoAcaoPontuacao : do tipo
```

## Cardinalidades resumidas

| Relacionamento | Tipo | Detalhe |
|----------------|------|---------|
| Tutor ↔ Pet | 1:N | Um tutor tem vários pets |
| Tutor ↔ HistoricoPontuacao | 1:N | Audit trail das ações |
| Tutor ↔ TransacaoParceiro | 1:N | Histórico de compras |
| Pet ↔ Vacina | 1:N | Histórico vacinal |
| Pet ↔ Consulta | 1:N | Agenda + histórico clínico |
| PetShop ↔ TransacaoParceiro | 1:N | Vendas do parceiro |
| Consulta ↔ PetShop | N:0..1 | Opcional (consulta pode ser em outro local) |

## Coerência DER ↔ Classes

Cada **entidade JPA** corresponde a uma **tabela no Oracle**:

| Classe Java | Tabela Oracle | PK |
|-------------|---------------|-----|
| `Tutor` | `TB_TUTOR` | `ID_TUTOR` |
| `Pet` | `TB_PET` | `ID_PET` |
| `Vacina` | `TB_VACINA` | `ID_VACINA` |
| `Consulta` | `TB_CONSULTA` | `ID_CONSULTA` |
| `PetShop` | `TB_PETSHOP` | `ID_PETSHOP` |
| `TransacaoParceiro` | `TB_TRANSACAO_PARCEIRO` | `ID_TRANSACAO` |
| `HistoricoPontuacao` | `TB_HISTORICO_PONTUACAO` | `ID_HISTORICO` |

### Constraints implementadas

- **PK** em todas as tabelas (`GENERATED ALWAYS AS IDENTITY`)
- **FK** em todos os relacionamentos (com nome explícito via `@ForeignKey`)
- **UNIQUE** em campos críticos: `Tutor.email`, `Tutor.cpf`, `PetShop.cnpj`
- **CHECK** em enums (`NIVEL`, `PLANO`, `ESPECIE`, `STATUS`)
- **NOT NULL** em campos obrigatórios
- **DEFAULT** em campos com valor inicial (`PONTOS_TOTAIS=0`, `ATIVO=1`)

## Normalização

O modelo está na **3ª Forma Normal (3FN)**:

- ✅ Todos os atributos são atômicos (1FN)
- ✅ Não há dependências parciais — toda PK é simples (2FN)
- ✅ Não há dependências transitivas — nível é derivado de pontosTotais via código, não armazenado redundantemente
