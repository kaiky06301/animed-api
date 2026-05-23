package br.com.fiap.clyvovet.dto;

import br.com.fiap.clyvovet.enums.StatusConsulta;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ConsultaDTO {

    public record Request(
            @NotNull(message = "Data/hora da consulta é obrigatória")
            LocalDateTime dataHora,

            @NotBlank(message = "Motivo é obrigatório")
            @Size(min = 5, max = 250)
            String motivo,

            @Size(max = 1000)
            String diagnostico,

            @Size(max = 1000)
            String prescricao,

            @DecimalMin(value = "0.0", message = "Valor não pode ser negativo")
            BigDecimal valor,

            StatusConsulta status,

            @Size(max = 120)
            String veterinario,

            @NotNull(message = "ID do pet é obrigatório")
            Long idPet,

            Long idPetShop
    ) {}

    public record Response(
            Long id,
            LocalDateTime dataHora,
            String motivo,
            String diagnostico,
            String prescricao,
            BigDecimal valor,
            StatusConsulta status,
            String veterinario,
            Long idPet,
            String nomePet,
            Long idPetShop,
            String nomePetShop
    ) {}
}
