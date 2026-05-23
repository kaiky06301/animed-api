package br.com.fiap.clyvovet.repository;

import br.com.fiap.clyvovet.entity.HistoricoPontuacao;
import br.com.fiap.clyvovet.enums.TipoAcaoPontuacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoricoPontuacaoRepository extends JpaRepository<HistoricoPontuacao, Long> {

    Page<HistoricoPontuacao> findByTutorIdOrderByDataHoraDesc(Long idTutor, Pageable pageable);

    Page<HistoricoPontuacao> findByTipoAcao(TipoAcaoPontuacao tipo, Pageable pageable);
}
