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
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    /** Consultas de um intervalo - usada para calcular horários livres. */
    List<Consulta> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    /** Agenda do veterinário em um intervalo, na ordem do expediente. */
    @Query("SELECT c FROM Consulta c JOIN FETCH c.pet p JOIN FETCH p.tutor "
            + "WHERE c.veterinarioResponsavel.id = :idVeterinario "
            + "AND c.dataHora BETWEEN :inicio AND :fim ORDER BY c.dataHora")
    List<Consulta> findAgendaDoVeterinario(@Param("idVeterinario") Long idVeterinario,
                                           @Param("inicio") LocalDateTime inicio,
                                           @Param("fim") LocalDateTime fim);


    Page<Consulta> findByPetId(Long idPet, Pageable pageable);

    /** Atendimentos de um pet dentro de um intervalo. */
    List<Consulta> findByPetIdAndDataHoraBetween(Long idPet,
                                                 LocalDateTime inicio,
                                                 LocalDateTime fim);

    Page<Consulta> findByStatus(StatusConsulta status, Pageable pageable);

    @Query("SELECT c FROM Consulta c WHERE c.dataHora BETWEEN :inicio AND :fim")
    Page<Consulta> findByPeriodo(@Param("inicio") LocalDateTime inicio,
                                 @Param("fim") LocalDateTime fim,
                                 Pageable pageable);

    /** Atendimentos do tutor em um horário exato, de qualquer pet dele. */
    @Query("SELECT c FROM Consulta c JOIN FETCH c.pet p "
            + "WHERE p.tutor.id = :idTutor AND c.dataHora = :dataHora")
    List<Consulta> findDoTutorNoHorario(@Param("idTutor") Long idTutor,
                                        @Param("dataHora") LocalDateTime dataHora);

    @Query("SELECT c FROM Consulta c WHERE c.pet.tutor.id = :idTutor ORDER BY c.dataHora DESC")
    Page<Consulta> findByTutor(@Param("idTutor") Long idTutor, Pageable pageable);
}
