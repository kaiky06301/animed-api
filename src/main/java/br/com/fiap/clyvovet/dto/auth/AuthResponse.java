package br.com.fiap.clyvovet.dto.auth;

import br.com.fiap.clyvovet.enums.Role;

/** Resposta devolvida ao aplicativo após autenticação bem-sucedida. */
public record AuthResponse(
        String token,
        String tipo,
        Long expiraEmMs,
        Long idUsuario,
        Long idTutor,
        String nome,
        String email,
        Role role
) {}
