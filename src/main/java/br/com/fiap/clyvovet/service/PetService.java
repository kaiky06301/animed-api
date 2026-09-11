package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.PetDTO;
import br.com.fiap.clyvovet.entity.Pet;
import br.com.fiap.clyvovet.entity.Tutor;
import br.com.fiap.clyvovet.enums.Especie;
import br.com.fiap.clyvovet.enums.TipoAcaoPontuacao;
import br.com.fiap.clyvovet.exception.BusinessException;
import br.com.fiap.clyvovet.exception.ResourceNotFoundException;
import br.com.fiap.clyvovet.repository.HistoricoPontuacaoRepository;
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
// Leitura transacional por padrão: mantém a sessão aberta durante o
// mapeamento para DTO (open-in-view está desligado). Os métodos de
// escrita declaram o próprio @Transactional, que prevalece.
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;
    private final HistoricoPontuacaoRepository historicoRepository;
    private final TutorService tutorService;
    private final GamificacaoService gamificacaoService;

    /** Máximo de pets por tutor. Acima disso o caso é de plano específico. */
    private static final int LIMITE_PETS_POR_TUTOR = 5;

    /**
     * Intervalo mínimo entre duas pesagens que rendem pontos.
     *
     * O peso de um animal não muda de forma relevante de um dia para o
     * outro: pontuar cada digitação transformaria a balança em uma fonte
     * infinita de pontos. O registro continua sendo aceito a qualquer
     * momento — o que fica limitado é o crédito.
     */
    private static final int DIAS_ENTRE_PESAGENS_PONTUADAS = 7;

    @Transactional
    @CacheEvict(value = "pets", allEntries = true)
    public PetDTO.Response criar(PetDTO.Request request) {
        Tutor tutor = tutorService.buscarEntidade(request.idTutor());

        long petsExistentes = petRepository.countByTutorId(tutor.getId());

        if (petsExistentes >= LIMITE_PETS_POR_TUTOR) {
            throw new BusinessException(
                    "Limite de " + LIMITE_PETS_POR_TUTOR + " pets por tutor atingido");
        }

        // Os pontos de cadastro valem apenas para o primeiro pet: do segundo em
        // diante o pet existe para registrar cuidados, que continuam pontuando.
        boolean primeiroPet = petsExistentes == 0;

        Pet pet = Pet.builder()
                .nome(request.nome())
                .especie(request.especie())
                .sexo(request.sexo())
                .raca(request.raca())
                .dataNascimento(request.dataNascimento())
                .pesoKg(request.pesoKg())
                .castrado(request.castrado() != null ? request.castrado() : false)
                .observacoesSaude(request.observacoesSaude())
                .tutor(tutor)
                .build();

        Pet salvo = petRepository.save(pet);

        if (primeiroPet) {
            gamificacaoService.registrarAcao(
                    tutor.getId(),
                    TipoAcaoPontuacao.CADASTRO_PET,
                    "Cadastro do pet " + pet.getNome()
            );

            // Bônus de perfil completo, também restrito ao primeiro pet
            if (isPerfilCompleto(pet)) {
                gamificacaoService.registrarAcao(
                        tutor.getId(),
                        TipoAcaoPontuacao.PERFIL_COMPLETO,
                        "Perfil completo do pet " + pet.getNome()
                );
            }
        }

        return toResponse(salvo);
    }

    /**
     * Registra que o pet ganhou foto e credita os pontos correspondentes.
     *
     * Assim como o cadastro, a foto só pontua no primeiro pet do tutor, e
     * apenas uma vez — trocar a foto depois não gera novos pontos.
     *
     * @return quantos pontos foram creditados (zero quando não houve crédito)
     */
    @Transactional
    public int registrarFoto(Long idPet) {
        Pet pet = buscarEntidade(idPet);
        Long idTutor = pet.getTutor().getId();

        boolean jaRecebeu = historicoRepository.existsByTutorIdAndTipoAcao(
                idTutor, TipoAcaoPontuacao.FOTO_PET);

        if (jaRecebeu) {
            return 0;
        }

        // Só o primeiro pet cadastrado do tutor rende os pontos da foto
        Pet primeiroPet = petRepository.findFirstByTutorIdOrderByIdAsc(idTutor).orElse(null);

        if (primeiroPet == null || !primeiroPet.getId().equals(idPet)) {
            return 0;
        }

        gamificacaoService.registrarAcao(
                idTutor,
                TipoAcaoPontuacao.FOTO_PET,
                "Foto adicionada ao pet " + pet.getNome()
        );

        return TipoAcaoPontuacao.FOTO_PET.getPontosPadrao();
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
        pet.setSexo(request.sexo());
        pet.setRaca(request.raca());
        pet.setDataNascimento(request.dataNascimento());
        pet.setPesoKg(request.pesoKg());
        pet.setCastrado(request.castrado() != null ? request.castrado() : pet.getCastrado());
        pet.setObservacoesSaude(request.observacoesSaude());

        // O peso informado na edição vale como pesagem, com a mesma regra
        // aplicada ao registro feito pela tela de cuidados.
        registrarPeso(pet, pesoAntigo, request.pesoKg());

        return toResponse(petRepository.save(pet));
    }

    /**
     * Credita a atualização de peso, respeitando o intervalo mínimo.
     *
     * @return pontos creditados; zero quando o peso não mudou ou quando a
     *         pesagem anterior ainda é recente demais.
     */
    @Transactional
    public int registrarPeso(Pet pet, BigDecimal pesoAntigo, BigDecimal pesoNovo) {
        if (pesoNovo == null || pesoNovo.compareTo(pesoAntigo == null
                ? BigDecimal.valueOf(-1) : pesoAntigo) == 0) {
            return 0;
        }

        LocalDate ultima = pet.getDataUltimaPesagem();
        LocalDate hoje = LocalDate.now();

        if (ultima != null && ultima.plusDays(DIAS_ENTRE_PESAGENS_PONTUADAS).isAfter(hoje)) {
            return 0;
        }

        pet.setDataUltimaPesagem(hoje);

        gamificacaoService.registrarAcao(
                pet.getTutor().getId(),
                TipoAcaoPontuacao.ATUALIZACAO_PESO,
                "Atualização de peso do pet " + pet.getNome()
        );

        return TipoAcaoPontuacao.ATUALIZACAO_PESO.getPontosPadrao();
    }

    /** Quando a próxima pesagem volta a render pontos. */
    public LocalDate proximaPesagemPontuada(Pet pet) {
        LocalDate ultima = pet.getDataUltimaPesagem();
        return ultima == null ? LocalDate.now()
                : ultima.plusDays(DIAS_ENTRE_PESAGENS_PONTUADAS);
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
                pet.getSexo(),
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
