package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.entity.Consulta;
import br.com.fiap.clyvovet.enums.StatusConsulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    Page<Consulta> findByPetId(Long idPet, Pageable pageable);

    Page<Consulta> findByStatus(StatusConsulta status, Pageable pageable);

    @Query("SELECT c FROM Consulta c WHERE c.dataHora BETWEEN :inicio AND :fim")
    Page<Consulta> findByPeriodo(@Param("inicio") LocalDateTime inicio,
                                 @Param("fim") LocalDateTime fim,
                                 Pageable pageable);

    @Query("SELECT c FROM Consulta c WHERE c.pet.tutor.id = :idTutor ORDER BY c.dataHora DESC")
    Page<Consulta> findByTutor(@Param("idTutor") Long idTutor, Pageable pageable);
}
