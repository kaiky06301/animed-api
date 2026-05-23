package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.PetDTO;
import br.com.fiap.clyvovet.entity.Pet;
import br.com.fiap.clyvovet.entity.Tutor;
import br.com.fiap.clyvovet.enums.Especie;
import br.com.fiap.clyvovet.enums.TipoAcaoPontuacao;
import br.com.fiap.clyvovet.exception.ResourceNotFoundException;
import br.com.fiap.clyvovet.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final TutorService tutorService;
    private final GamificacaoService gamificacaoService;

    @Transactional
    @CacheEvict(value = "pets", allEntries = true)
    public PetDTO.Response criar(PetDTO.Request request) {
        Tutor tutor = tutorService.buscarEntidade(request.idTutor());

        Pet pet = Pet.builder()
                .nome(request.nome())
                .especie(request.especie())
                .raca(request.raca())
                .dataNascimento(request.dataNascimento())
                .pesoKg(request.pesoKg())
                .castrado(request.castrado() != null ? request.castrado() : false)
                .observacoesSaude(request.observacoesSaude())
                .tutor(tutor)
                .build();

        Pet salvo = petRepository.save(pet);

        // Gamificação: cadastrar pet rende pontos
        gamificacaoService.registrarAcao(
                tutor.getId(),
                TipoAcaoPontuacao.CADASTRO_PET,
                "Cadastro do pet " + pet.getNome()
        );

        // Bônus de perfil completo
        if (isPerfilCompleto(pet)) {
            gamificacaoService.registrarAcao(
                    tutor.getId(),
                    TipoAcaoPontuacao.PERFIL_COMPLETO,
                    "Perfil completo do pet " + pet.getNome()
            );
        }

        return toResponse(salvo);
    }

    @Cacheable(value = "pets", key = "#id")
    public PetDTO.Response buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    public Pet buscarEntidade(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", id));
    }

    public Page<PetDTO.Response> listar(Pageable pageable) {
        return petRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<PetDTO.Response> listarPorTutor(Long idTutor, Pageable pageable) {
        return petRepository.findByTutorId(idTutor, pageable).map(this::toResponse);
    }

    public Page<PetDTO.Response> buscarPorEspecie(Especie especie, Pageable pageable) {
        return petRepository.findByEspecie(especie, pageable).map(this::toResponse);
    }

    public Page<PetDTO.Response> buscarPorNome(String nome, Pageable pageable) {
        return petRepository.findByNomeContainingIgnoreCase(nome, pageable).map(this::toResponse);
    }

    @Transactional
    @CacheEvict(value = "pets", allEntries = true)
    public PetDTO.Response atualizar(Long id, PetDTO.Request request) {
        Pet pet = buscarEntidade(id);
        BigDecimal pesoAntigo = pet.getPesoKg();

        pet.setNome(request.nome());
        pet.setEspecie(request.especie());
        pet.setRaca(request.raca());
        pet.setDataNascimento(request.dataNascimento());
        pet.setPesoKg(request.pesoKg());
        pet.setCastrado(request.castrado() != null ? request.castrado() : pet.getCastrado());
        pet.setObservacoesSaude(request.observacoesSaude());

        Pet atualizado = petRepository.save(pet);

        // Se o peso foi atualizado, gera pontos
        if (request.pesoKg() != null && !request.pesoKg().equals(pesoAntigo)) {
            gamificacaoService.registrarAcao(
                    pet.getTutor().getId(),
                    TipoAcaoPontuacao.ATUALIZACAO_PESO,
                    "Atualização de peso do pet " + pet.getNome()
            );
        }

        return toResponse(atualizado);
    }

    @Transactional
    @CacheEvict(value = "pets", allEntries = true)
    public void deletar(Long id) {
        if (!petRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pet", id);
        }
        petRepository.deleteById(id);
    }

    private boolean isPerfilCompleto(Pet pet) {
        return pet.getRaca() != null && !pet.getRaca().isBlank()
                && pet.getDataNascimento() != null
                && pet.getPesoKg() != null
                && pet.getObservacoesSaude() != null && !pet.getObservacoesSaude().isBlank();
    }

    private PetDTO.Response toResponse(Pet pet) {
        Integer idade = pet.getDataNascimento() != null
                ? Period.between(pet.getDataNascimento(), LocalDate.now()).getYears()
                : null;

        return new PetDTO.Response(
                pet.getId(),
                pet.getNome(),
                pet.getEspecie(),
                pet.getRaca(),
                pet.getDataNascimento(),
                idade,
                pet.getPesoKg(),
                pet.getCastrado(),
                pet.getObservacoesSaude(),
                pet.getTutor().getId(),
                pet.getTutor().getNome()
        );
    }
}
