package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.VacinaDTO;
import br.com.fiap.clyvovet.entity.Pet;
import br.com.fiap.clyvovet.entity.Vacina;
import br.com.fiap.clyvovet.enums.TipoAcaoPontuacao;
import br.com.fiap.clyvovet.exception.ResourceNotFoundException;
import br.com.fiap.clyvovet.repository.VacinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VacinaService {

    private final VacinaRepository vacinaRepository;
    private final PetService petService;
    private final GamificacaoService gamificacaoService;

    @Transactional
    public VacinaDTO.Response criar(VacinaDTO.Request request) {
        Pet pet = petService.buscarEntidade(request.idPet());

        Vacina vacina = Vacina.builder()
                .nomeVacina(request.nomeVacina())
                .dataAplicacao(request.dataAplicacao())
                .dataProximaDose(request.dataProximaDose())
                .veterinarioResponsavel(request.veterinarioResponsavel())
                .clinica(request.clinica())
                .lote(request.lote())
                .observacoes(request.observacoes())
                .pet(pet)
                .build();

        Vacina salva = vacinaRepository.save(vacina);

        // Gamificação: vacinação rende pontos
        gamificacaoService.registrarAcao(
                pet.getTutor().getId(),
                TipoAcaoPontuacao.REGISTRO_VACINA,
                "Vacinação: " + request.nomeVacina() + " - " + pet.getNome()
        );

        return toResponse(salva);
    }

    public VacinaDTO.Response buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    public Vacina buscarEntidade(Long id) {
        return vacinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacina", id));
    }

    public Page<VacinaDTO.Response> listar(Pageable pageable) {
        return vacinaRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<VacinaDTO.Response> listarPorPet(Long idPet, Pageable pageable) {
        return vacinaRepository.findByPetId(idPet, pageable).map(this::toResponse);
    }

    public Page<VacinaDTO.Response> listarPorTutor(Long idTutor, Pageable pageable) {
        return vacinaRepository.findByTutor(idTutor, pageable).map(this::toResponse);
    }

    @Transactional
    public VacinaDTO.Response atualizar(Long id, VacinaDTO.Request request) {
        Vacina vacina = buscarEntidade(id);

        vacina.setNomeVacina(request.nomeVacina());
        vacina.setDataAplicacao(request.dataAplicacao());
        vacina.setDataProximaDose(request.dataProximaDose());
        vacina.setVeterinarioResponsavel(request.veterinarioResponsavel());
        vacina.setClinica(request.clinica());
        vacina.setLote(request.lote());
        vacina.setObservacoes(request.observacoes());

        return toResponse(vacinaRepository.save(vacina));
    }

    @Transactional
    public void deletar(Long id) {
        if (!vacinaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vacina", id);
        }
        vacinaRepository.deleteById(id);
    }

    private VacinaDTO.Response toResponse(Vacina v) {
        return new VacinaDTO.Response(
                v.getId(),
                v.getNomeVacina(),
                v.getDataAplicacao(),
                v.getDataProximaDose(),
                v.getVeterinarioResponsavel(),
                v.getClinica(),
                v.getLote(),
                v.getObservacoes(),
                v.getPet().getId(),
                v.getPet().getNome()
        );
    }
}
