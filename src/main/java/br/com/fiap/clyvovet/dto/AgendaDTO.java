package br.com.fiap.clyvovet.dto;

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
            int pontosGanhos,
            List<String> orientacoes
    ) {}
}
