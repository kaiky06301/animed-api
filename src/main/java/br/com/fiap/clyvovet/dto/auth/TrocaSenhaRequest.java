package br.com.fiap.clyvovet.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Troca de senha feita pelo próprio usuário autenticado. */
public record TrocaSenhaRequest(

        @NotBlank(message = "Informe a senha atual")
        String senhaAtual,

        @NotBlank(message = "Informe a nova senha")
        @Size(min = 6, max = 60, message = "A nova senha deve ter ao menos 6 caracteres")
        String novaSenha
) {}
