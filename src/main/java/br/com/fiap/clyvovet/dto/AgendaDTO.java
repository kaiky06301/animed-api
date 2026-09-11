package br.com.fiap.clyvovet.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Agenda de atendimentos da clínica. */
public class AgendaDTO {

    /** Horários livres de um dia. */
    public record Disponibilidade(
            LocalDate data,
            boolean atende,
            String veterinario,
            String observacao,
            List<String> horarios,
            int duracaoMinutos,
            String clinica,
            String endereco
    ) {}

    /** Um dia do calendário e se ainda há horário para marcar. */
    public record DiaDoMes(
            LocalDate data,
            boolean disponivel,
            int horariosLivres
    ) {}

    /** Calendário do mês, para destacar os dias com vaga. */
    public record MesDisponivel(
            int ano,
            int mes,
            List<DiaDoMes> dias
    ) {}

    /** Um horário já reservado na agenda do veterinário. */
    public record Atendimento(
            Long idConsulta,
            String horario,
            Long idPet,
            String nomePet,
            String nomeTutor,
            String motivo,
            String status
    ) {}

    /**
     * Fechamento do atendimento.
     *
     * O retorno é decisão clínica: só o veterinário diz se haverá e quando.
     */
    public record Conclusao(
            @Future(message = "O retorno deve ser marcado para uma data futura")
            LocalDateTime retorno,

            /** Instrução que o tutor deve seguir até o retorno. */
            @Size(max = 500)
            String orientacao,

            /** O que foi encontrado no atendimento. */
            @Size(max = 1000)
            String diagnostico,

            /** Conduta indicada, em texto livre. */
            @Size(max = 1000)
            String prescricao
    ) {}

    /** Resultado do fechamento: o atendimento e o retorno, quando houver. */
    public record AtendimentoConcluido(
            Atendimento atendimento,
            LocalDateTime retorno,
            int pontosCreditados
    ) {}

    /** Tudo o que o tutor precisa saber sobre um atendimento. */
    public record DetalheAtendimento(
            Long idConsulta,
            LocalDateTime dataHora,
            String motivo,
            String status,
            String veterinario,
            String clinica,
            String endereco,
            int duracaoMinutos,
            Long idPet,
            String nomePet,
            String diagnostico,
            String prescricao,
            String orientacao
    ) {}

    /** Resultado do cancelamento, com o que saiu da pontuação. */
    public record AtendimentoCancelado(
            Long idConsulta,
            LocalDateTime dataHora,
            String motivo,
            int pontosEstornados
    ) {}

    /** O dia inteiro como o veterinário o enxerga. */
    public record AgendaDoDia(
            LocalDate data,
            String veterinario,
            int horariosLivres,
            List<Atendimento> atendimentos
    ) {}

    public record Agendamento(
            @NotNull(message = "ID do pet é obrigatório")
            Long idPet,

            @NotNull(message = "Data e hora são obrigatórias")
            @FutureOrPresent(message = "O atendimento deve ser agendado para uma data futura")
            LocalDateTime dataHora,

            @NotNull(message = "Motivo é obrigatório")
            @Size(max = 250)
            String motivo
    ) {}

    public record AgendamentoConfirmado(
            Long idConsulta,
            LocalDateTime dataHora,
            String motivo,
            String veterinario,
            int pontosGanhos
    ) {}
}
