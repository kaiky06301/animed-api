package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.HistoricoPontuacaoDTO;
import br.com.fiap.clyvovet.entity.HistoricoPontuacao;
import br.com.fiap.clyvovet.enums.TipoAcaoPontuacao;
import br.com.fiap.clyvovet.repository.HistoricoPontuacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
// Leitura transacional por padrão: mantém a sessão aberta durante o
// mapeamento para DTO (open-in-view está desligado). Os métodos de
// escrita declaram o próprio @Transactional, que prevalece.
@Transactional(readOnly = true)
public class HistoricoPontuacaoService {

    private final HistoricoPontuacaoRepository historicoRepository;

    public Page<HistoricoPontuacaoDTO.Response> listar(Pageable pageable) {
        return historicoRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<HistoricoPontuacaoDTO.Response> listarPorTutor(Long idTutor, Pageable pageable) {
        return historicoRepository.findByTutorIdOrderByDataHoraDesc(idTutor, pageable).map(this::toResponse);
    }

    public Page<HistoricoPontuacaoDTO.Response> listarPorTipo(TipoAcaoPontuacao tipo, Pageable pageable) {
        return historicoRepository.findByTipoAcao(tipo, pageable).map(this::toResponse);
    }

    private HistoricoPontuacaoDTO.Response toResponse(HistoricoPontuacao h) {
        return new HistoricoPontuacaoDTO.Response(
                h.getId(),
                h.getTipoAcao(),
                h.getTipoAcao().getDescricao(),
                h.getPontosGanhos(),
                h.getDescricao(),
                h.getDataHora(),
                h.getTutor().getId()
        );
    }
}
