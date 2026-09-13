package br.com.fiap.clyvovet.dto;

import br.com.fiap.clyvovet.enums.Role;

import java.time.LocalDateTime;

/**
 * Contas de acesso, na visão de quem administra a clínica.
 *
 * A senha nunca aparece aqui — nem o hash. Quem administra precisa saber quem
 * tem acesso e a que, não como a pessoa entra.
 */
public class UsuarioDTO {

    public record Response(
            Long id,
            String nome,
            String email,
            Role role,
            boolean ativo,
            LocalDateTime dataCadastro,

            /** Vínculo com o tutor, quando o perfil é TUTOR. */
            Long idTutor,

            /** Quantos pets estão sob responsabilidade deste tutor. */
            int pets
    ) {}

    /** Ligar ou desligar o acesso de alguém. */
    public record MudancaDeAcesso(boolean ativo) {}
}
