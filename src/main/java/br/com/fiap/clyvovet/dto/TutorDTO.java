package br.com.fiap.clyvovet.dto;

import br.com.fiap.clyvovet.enums.NivelGamificacao;
import br.com.fiap.clyvovet.enums.PlanoAssinatura;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class TutorDTO {

    public record Request(
            @NotBlank(message = "Nome é obrigatório")
            @Size(min = 3, max = 120, message = "Nome deve ter entre 3 e 120 caracteres")
            String nome,

            @NotBlank(message = "Email é obrigatório")
            @Email(message = "Email inválido")
            @Size(max = 150)
            String email,

            @NotBlank(message = "CPF é obrigatório")
            @Pattern(regexp = "\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}", message = "CPF inválido")
            String cpf,

            @Pattern(regexp = "^$|\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}", message = "Telefone inválido")
            String telefone,

            PlanoAssinatura plano
    ) {}

    public record Response(
            Long id,
            String nome,
            String email,
            String cpf,
            String telefone,
            Integer pontosTotais,
            NivelGamificacao nivel,
            String nivelDescricao,
            Integer descontoPercentual,
            PlanoAssinatura plano,
            LocalDateTime dataCadastro,
            Boolean ativo
    ) {}
}
