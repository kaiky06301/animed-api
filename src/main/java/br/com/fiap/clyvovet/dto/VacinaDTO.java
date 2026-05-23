package br.com.fiap.clyvovet.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class VacinaDTO {

    public record Request(
            @NotBlank(message = "Nome da vacina é obrigatório")
            @Size(max = 100)
            String nomeVacina,

            @NotNull(message = "Data de aplicação é obrigatória")
            @PastOrPresent(message = "Data de aplicação não pode ser futura")
            LocalDate dataAplicacao,

            @Future(message = "Próxima dose deve ser no futuro")
            LocalDate dataProximaDose,

            @Size(max = 120)
            String veterinarioResponsavel,

            @Size(max = 150)
            String clinica,

            @Size(max = 50)
            String lote,

            @Size(max = 500)
            String observacoes,

            @NotNull(message = "ID do pet é obrigatório")
            Long idPet
    ) {}

    public record Response(
            Long id,
            String nomeVacina,
            LocalDate dataAplicacao,
            LocalDate dataProximaDose,
            String veterinarioResponsavel,
            String clinica,
            String lote,
            String observacoes,
            Long idPet,
            String nomePet
    ) {}
}
