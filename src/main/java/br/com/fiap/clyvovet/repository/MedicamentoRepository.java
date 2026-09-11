package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    /** Prescrições do pet, da mais recente para a mais antiga. */
    @Query("SELECT m FROM Medicamento m WHERE m.pet.id = :idPet ORDER BY m.dataInicio DESC")
    List<Medicamento> findByPet(@Param("idPet") Long idPet);
}
