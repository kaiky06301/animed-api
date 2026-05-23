package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.entity.Tutor;
import br.com.fiap.clyvovet.enums.NivelGamificacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {

    Optional<Tutor> findByEmail(String email);

    Optional<Tutor> findByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    Page<Tutor> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Tutor> findByNivel(NivelGamificacao nivel, Pageable pageable);

    // JPQL - Top tutores por pontos
    @Query("SELECT t FROM Tutor t WHERE t.ativo = true ORDER BY t.pontosTotais DESC")
    Page<Tutor> findTopByPontos(Pageable pageable);

    @Query("SELECT t FROM Tutor t WHERE t.pontosTotais >= :pontosMin AND t.pontosTotais <= :pontosMax")
    Page<Tutor> findByFaixaPontos(@Param("pontosMin") Integer min,
                                  @Param("pontosMax") Integer max,
                                  Pageable pageable);
}
