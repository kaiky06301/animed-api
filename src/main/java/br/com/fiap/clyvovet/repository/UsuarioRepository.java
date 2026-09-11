package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.entity.Usuario;
import br.com.fiap.clyvovet.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Veterinário da clínica, dono da agenda de atendimentos. */
    Optional<Usuario> findFirstByRoleAndAtivoTrueOrderByIdAsc(Role role);
}
