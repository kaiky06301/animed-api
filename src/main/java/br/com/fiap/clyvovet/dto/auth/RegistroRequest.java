package br.com.fiap.clyvovet.dto.auth;

import br.com.fiap.clyvovet.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados para criação de uma nova credencial de acesso. */
public record RegistroRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        String senha,

        @NotBlank(message = "CPF é obrigatório")
        String cpf,

        String telefone,

        @NotNull(message = "Perfil é obrigatório")
        Role role
) {}
