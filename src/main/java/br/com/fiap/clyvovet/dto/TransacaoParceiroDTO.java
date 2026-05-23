package br.com.fiap.clyvovet.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransacaoParceiroDTO {

    public record Request(
            @NotNull(message = "Valor bruto é obrigatório")
            @DecimalMin(value = "0.01", message = "Valor bruto deve ser maior que zero")
            BigDecimal valorBruto,

            @Size(max = 250)
            String descricaoProduto,

            @NotNull(message = "ID do tutor é obrigatório")
            Long idTutor,

            @NotNull(message = "ID do pet shop é obrigatório")
            Long idPetShop
    ) {}

    public record Response(
            Long id,
            LocalDateTime dataHora,
            BigDecimal valorBruto,
            BigDecimal descontoAplicado,
            BigDecimal valorFinal,
            BigDecimal comissaoClyvo,
            Integer pontosGerados,
            String descricaoProduto,
            Long idTutor,
            String nomeTutor,
            Long idPetShop,
            String nomePetShop
    ) {}
}
