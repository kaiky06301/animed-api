package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.entity.Vacina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacinaRepository extends JpaRepository<Vacina, Long> {

    Page<Vacina> findByPetId(Long idPet, Pageable pageable);

    @Query("SELECT v FROM Vacina v WHERE v.dataProximaDose BETWEEN :inicio AND :fim")
    List<Vacina> findProximasDoses(@Param("inicio") LocalDate inicio,
                                   @Param("fim") LocalDate fim);

    @Query("SELECT v FROM Vacina v WHERE v.pet.tutor.id = :idTutor ORDER BY v.dataAplicacao DESC")
    Page<Vacina> findByTutor(@Param("idTutor") Long idTutor, Pageable pageable);
}
