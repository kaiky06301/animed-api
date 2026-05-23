package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.TutorDTO;
import br.com.fiap.clyvovet.entity.Tutor;
import br.com.fiap.clyvovet.enums.NivelGamificacao;
import br.com.fiap.clyvovet.enums.PlanoAssinatura;
import br.com.fiap.clyvovet.exception.BusinessException;
import br.com.fiap.clyvovet.exception.ResourceNotFoundException;
import br.com.fiap.clyvovet.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TutorService {

    private final TutorRepository tutorRepository;

    @Transactional
    @CacheEvict(value = "tutores", allEntries = true)
    public TutorDTO.Response criar(TutorDTO.Request request) {
        if (tutorRepository.existsByEmail(request.email())) {
            throw new BusinessException("Já existe um tutor com este email: " + request.email());
        }
        if (tutorRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("Já existe um tutor com este CPF.");
        }

        Tutor tutor = Tutor.builder()
                .nome(request.nome())
                .email(request.email())
                .cpf(request.cpf())
                .telefone(request.telefone())
                .plano(request.plano() != null ? request.plano() : PlanoAssinatura.GRATUITO)
                .pontosTotais(0)
                .nivel(NivelGamificacao.BASICO)
                .ativo(true)
                .build();

        return toResponse(tutorRepository.save(tutor));
    }

    @Cacheable(value = "tutores", key = "#id")
    public TutorDTO.Response buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    public Tutor buscarEntidade(Long id) {
        return tutorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", id));
    }

    @Cacheable(value = "tutores-lista", key = "#pageable")
    public Page<TutorDTO.Response> listar(Pageable pageable) {
        return tutorRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<TutorDTO.Response> buscarPorNome(String nome, Pageable pageable) {
        return tutorRepository.findByNomeContainingIgnoreCase(nome, pageable).map(this::toResponse);
    }

    public Page<TutorDTO.Response> buscarPorNivel(NivelGamificacao nivel, Pageable pageable) {
        return tutorRepository.findByNivel(nivel, pageable).map(this::toResponse);
    }

    public Page<TutorDTO.Response> ranking(Pageable pageable) {
        return tutorRepository.findTopByPontos(pageable).map(this::toResponse);
    }

    @Transactional
    @CacheEvict(value = {"tutores", "tutores-lista"}, allEntries = true)
    public TutorDTO.Response atualizar(Long id, TutorDTO.Request request) {
        Tutor tutor = buscarEntidade(id);

        tutor.setNome(request.nome());
        tutor.setEmail(request.email());
        tutor.setTelefone(request.telefone());
        if (request.plano() != null) tutor.setPlano(request.plano());

        return toResponse(tutorRepository.save(tutor));
    }

    @Transactional
    @CacheEvict(value = {"tutores", "tutores-lista"}, allEntries = true)
    public void deletar(Long id) {
        if (!tutorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tutor", id);
        }
        tutorRepository.deleteById(id);
    }

    private TutorDTO.Response toResponse(Tutor t) {
        return new TutorDTO.Response(
                t.getId(),
                t.getNome(),
                t.getEmail(),
                t.getCpf(),
                t.getTelefone(),
                t.getPontosTotais(),
                t.getNivel(),
                t.getNivel().getDescricao(),
                t.getNivel().getDescontoPercentual(),
                t.getPlano(),
                t.getDataCadastro(),
                t.getAtivo()
        );
    }
}
