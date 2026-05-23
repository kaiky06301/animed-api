package br.com.fiap.clyvovet.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PetShopDTO {

    public record Request(
            @NotBlank(message = "Razão social é obrigatória")
            @Size(max = 150)
            String razaoSocial,

            @NotBlank(message = "Nome fantasia é obrigatório")
            @Size(max = 120)
            String nomeFantasia,

            @NotBlank(message = "CNPJ é obrigatório")
            @Pattern(regexp = "\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}", message = "CNPJ inválido")
            String cnpj,

            @Size(max = 250)
            String endereco,

            @Size(max = 100)
            String cidade,

            @Size(min = 2, max = 2, message = "UF deve ter 2 letras")
            String uf,

            @Pattern(regexp = "^$|\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}", message = "Telefone inválido")
            String telefone,

            @DecimalMin(value = "0.0", message = "Taxa mensal não pode ser negativa")
            BigDecimal taxaMensal,

            @DecimalMin(value = "0.0", message = "Comissão não pode ser negativa")
            @DecimalMax(value = "100.0", message = "Comissão não pode ultrapassar 100%")
            BigDecimal comissaoPercentual
    ) {}

    public record Response(
            Long id,
            String razaoSocial,
            String nomeFantasia,
            String cnpj,
            String endereco,
            String cidade,
            String uf,
            String telefone,
            BigDecimal taxaMensal,
            BigDecimal comissaoPercentual,
            Boolean ativo,
            LocalDateTime dataParceria
    ) {}
}
