package br.com.fiap.clyvovet.dto;

import br.com.fiap.clyvovet.enums.Especie;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PetDTO {

    public record Request(
            @NotBlank(message = "Nome do pet é obrigatório")
            @Size(min = 1, max = 80)
            String nome,

            @NotNull(message = "Espécie é obrigatória")
            Especie especie,

            @Size(max = 80)
            String raca,

            @PastOrPresent(message = "Data de nascimento não pode ser no futuro")
            LocalDate dataNascimento,

            @DecimalMin(value = "0.1", message = "Peso deve ser maior que 0.1 kg")
            @DecimalMax(value = "200.0", message = "Peso deve ser menor que 200 kg")
            BigDecimal pesoKg,

            Boolean castrado,

            @Size(max = 1000)
            String observacoesSaude,

            @NotNull(message = "ID do tutor é obrigatório")
            Long idTutor
    ) {}

    public record Response(
            Long id,
            String nome,
            Especie especie,
            String raca,
            LocalDate dataNascimento,
            Integer idadeAnos,
            BigDecimal pesoKg,
            Boolean castrado,
            String observacoesSaude,
            Long idTutor,
            String nomeTutor
    ) {}
}
