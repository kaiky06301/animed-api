package br.com.fiap.clyvovet.enums;

/**
 * Perfis de acesso da aplicação.
 * O Animed tem dois tipos de usuário com permissões distintas:
 * o TUTOR (dono do pet) e o DOUTOR (veterinário).
 */
public enum Role {

    /** Dono do pet: gerencia os próprios pets e registra cuidados. */
    TUTOR,

    /** Veterinário: cadastra tutores e acompanha os pacientes da clínica. */
    DOUTOR
}
