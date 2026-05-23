package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.entity.HistoricoPontuacao;
import br.com.fiap.clyvovet.entity.Tutor;
import br.com.fiap.clyvovet.enums.TipoAcaoPontuacao;
import br.com.fiap.clyvovet.exception.ResourceNotFoundException;
import br.com.fiap.clyvovet.repository.HistoricoPontuacaoRepository;
import br.com.fiap.clyvovet.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service de Gamificação - Núcleo do diferencial Clyvo VET.
 * Responsável por aplicar pontos, atualizar níveis e registrar histórico.
 */
@Service
@RequiredArgsConstructor
public class GamificacaoService {

    private final TutorRepository tutorRepository;
    private final HistoricoPontuacaoRepository historicoRepository;

    /**
     * Registra uma ação que rende pontos ao tutor, atualiza nível e grava histórico.
     */
    @Transactional
    @CacheEvict(value = "tutores", allEntries = true)
    public void registrarAcao(Long idTutor, TipoAcaoPontuacao tipoAcao, String descricao) {
        Tutor tutor = tutorRepository.findById(idTutor)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", idTutor));

        int pontos = tipoAcao.getPontosPadrao();
        tutor.adicionarPontos(pontos);
        tutorRepository.save(tutor);

        HistoricoPontuacao hist = HistoricoPontuacao.builder()
                .tutor(tutor)
                .tipoAcao(tipoAcao)
                .pontosGanhos(pontos)
                .descricao(descricao != null ? descricao : tipoAcao.getDescricao())
                .build();

        historicoRepository.save(hist);
    }

    /**
     * Versão com pontos customizados (ex: missões com peso variável).
     */
    @Transactional
    @CacheEvict(value = "tutores", allEntries = true)
    public void registrarAcaoCustomizada(Long idTutor, TipoAcaoPontuacao tipoAcao, int pontosCustom, String descricao) {
        Tutor tutor = tutorRepository.findById(idTutor)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", idTutor));

        tutor.adicionarPontos(pontosCustom);
        tutorRepository.save(tutor);

        HistoricoPontuacao hist = HistoricoPontuacao.builder()
                .tutor(tutor)
                .tipoAcao(tipoAcao)
                .pontosGanhos(pontosCustom)
                .descricao(descricao)
                .build();

        historicoRepository.save(hist);
    }
}
