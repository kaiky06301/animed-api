package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.CuidadoDTO;
import br.com.fiap.clyvovet.entity.Pet;
import br.com.fiap.clyvovet.entity.Tutor;
import br.com.fiap.clyvovet.enums.TipoCuidadoTutor;
import br.com.fiap.clyvovet.exception.BusinessException;
import br.com.fiap.clyvovet.repository.PetRepository;
import br.com.fiap.clyvovet.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cuidados realizados pelo próprio tutor.
 *
 * Diferente dos registros clínicos — vacinação, consulta e check-up —,
 * estes acontecem no dia a dia em casa e por isso são lançados pelo tutor.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CuidadoService {

    private final PetService petService;
    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;
    private final GamificacaoService gamificacaoService;

    @Transactional
    public CuidadoDTO.Response registrar(CuidadoDTO.Request request) {
        Pet pet = petService.buscarEntidade(request.idPet());
        Tutor tutor = pet.getTutor();

        // A pesagem além de pontuar atualiza o dado do pet
        if (request.tipo() == TipoCuidadoTutor.PESAGEM) {
            if (request.pesoKg() == null) {
                throw new BusinessException("Informe o peso para registrar a pesagem");
            }
            pet.setPesoKg(request.pesoKg());
            petRepository.save(pet);
        }

        String descricao = request.observacao() != null && !request.observacao().isBlank()
                ? request.tipo().getDescricao() + ": " + request.observacao()
                : request.tipo().getDescricao() + " - " + pet.getNome();

        gamificacaoService.registrarAcao(tutor.getId(), request.tipo().getAcao(), descricao);

        Tutor atualizado = tutorRepository.findById(tutor.getId()).orElseThrow();

        return new CuidadoDTO.Response(
                request.tipo(),
                descricao,
                request.tipo().getAcao().getPontosPadrao(),
                atualizado.getPontosTotais(),
                atualizado.getMoedas()
        );
    }
}
