package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.entity.DoseMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoseMedicamentoRepository extends JpaRepository<DoseMedicamento, Long> {

    /** Última dose dada, para saber quando a próxima é permitida. */
    Optional<DoseMedicamento> findFirstByMedicamentoIdOrderByDataHoraDesc(Long idMedicamento);

    List<DoseMedicamento> findByMedicamentoIdOrderByDataHoraDesc(Long idMedicamento);

    long countByMedicamentoId(Long idMedicamento);
}
