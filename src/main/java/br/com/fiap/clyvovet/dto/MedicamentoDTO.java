package br.com.fiap.clyvovet.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Prescrição de medicamento e as doses dadas ao pet. */
public class MedicamentoDTO {

    /** O que o veterinário prescreve. */
    public record Request(
            @NotNull(message = "ID do pet é obrigatório")
            Long idPet,

            @NotBlank(message = "Nome do medicamento é obrigatório")
            @Size(max = 120)
            String nome,

            @Size(max = 60, message = "Dosagem deve ter no máximo 60 caracteres")
            String dosagem,

            @NotNull(message = "Informe de quantas em quantas horas o remédio deve ser dado")
            @Min(value = 1, message = "O intervalo deve ser de pelo menos 1 hora")
            @Max(value = 8760, message = "O intervalo deve ser menor que um ano")
            Integer intervaloHoras,

            @NotNull(message = "Data de início é obrigatória")
            LocalDate dataInicio,

            /** Nulo quando o uso é contínuo. */
            LocalDate dataFim,

            @Size(max = 250)
            String observacao
    ) {}

    /** Registro de uma dose dada pelo tutor. */
    public record DoseRequest(
            @Size(max = 250)
            String observacao
    ) {}

    public record Response(
            Long id,
            String nome,
            String dosagem,
            int intervaloHoras,
            /** "de 12 em 12 horas", "a cada 3 meses" */
            String posologia,
            LocalDate dataInicio,
            LocalDate dataFim,
            String observacao,
            String veterinario,
            Long idPet,
            String nomePet,

            /** Situação do tratamento hoje. */
            boolean emCurso,
            long dosesRegistradas,
            LocalDateTime ultimaDose,
            LocalDateTime proximaDose,
            /** true quando já passou a hora da próxima dose. */
            boolean doseLiberada
    ) {}

    /** Resultado do registro de uma dose. */
    public record DoseRegistrada(
            Long idDose,
            LocalDateTime dataHora,
            String medicamento,
            int pontosGanhos,
            int pontosTotais,
            LocalDateTime proximaDose
    ) {}
}
