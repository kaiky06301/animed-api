package br.com.fiap.clyvovet.dto;

import br.com.fiap.clyvovet.enums.TipoCuidadoTutor;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Registro de um cuidado feito pelo próprio tutor. */
public class CuidadoDTO {

    public record Request(
            @NotNull(message = "ID do pet é obrigatório")
            Long idPet,

            @NotNull(message = "Tipo de cuidado é obrigatório")
            TipoCuidadoTutor tipo,

            /** Obrigatório apenas quando o cuidado é a pesagem. */
            @DecimalMin(value = "0.1", message = "Peso deve ser maior que 0.1 kg")
            @DecimalMax(value = "200.0", message = "Peso deve ser menor que 200 kg")
            BigDecimal pesoKg,

            @Size(max = 250, message = "Observação deve ter no máximo 250 caracteres")
            String observacao
    ) {}

    public record Response(
            TipoCuidadoTutor tipo,
            String descricao,
            int pontosGanhos,
            int pontosTotais,
            int moedas,

            /** Explica por que a ação não rendeu pontos, quando for o caso. */
            String aviso
    ) {}
}
