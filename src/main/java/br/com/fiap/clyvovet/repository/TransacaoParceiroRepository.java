package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.entity.TransacaoParceiro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface TransacaoParceiroRepository extends JpaRepository<TransacaoParceiro, Long> {

    Page<TransacaoParceiro> findByTutorId(Long idTutor, Pageable pageable);

    Page<TransacaoParceiro> findByPetShopId(Long idPetShop, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.comissaoClyvo), 0) FROM TransacaoParceiro t WHERE t.petShop.id = :idPetShop")
    BigDecimal totalComissaoPorPetShop(@Param("idPetShop") Long idPetShop);

    @Query("SELECT COALESCE(SUM(t.valorFinal), 0) FROM TransacaoParceiro t WHERE t.tutor.id = :idTutor")
    BigDecimal totalGastoPorTutor(@Param("idTutor") Long idTutor);
}
