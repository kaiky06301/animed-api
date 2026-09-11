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

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

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

        String descricao = request.observacao() != null && !request.observacao().isBlank()
                ? request.tipo().getDescricao() + ": " + request.observacao()
                : request.tipo().getDescricao() + " - " + pet.getNome();

        int pontos;
        String aviso = null;

        if (request.tipo() == TipoCuidadoTutor.PESAGEM) {
            if (request.pesoKg() == null) {
                throw new BusinessException("Informe o peso para registrar a pesagem");
            }

            // O peso é sempre guardado; o crédito é que respeita o intervalo
            BigDecimal pesoAntigo = pet.getPesoKg();
            pontos = petService.registrarPeso(pet, pesoAntigo, request.pesoKg());
            pet.setPesoKg(request.pesoKg());
            petRepository.save(pet);

            if (pontos == 0) {
                aviso = pesoAntigo != null && pesoAntigo.compareTo(request.pesoKg()) == 0
                        ? "Esse já era o peso registrado"
                        : "Peso atualizado. A próxima pesagem rende pontos a partir de "
                                + petService.proximaPesagemPontuada(pet)
                                        .format(DateTimeFormatter.ofPattern("dd/MM"));
            }
        } else {
            gamificacaoService.registrarAcao(tutor.getId(), request.tipo().getAcao(), descricao);
            pontos = request.tipo().getAcao().getPontosPadrao();
        }

        Tutor atualizado = tutorRepository.findById(tutor.getId()).orElseThrow();

        return new CuidadoDTO.Response(
                request.tipo(),
                descricao,
                pontos,
                atualizado.getPontosTotais(),
                atualizado.getMoedas(),
                aviso
        );
    }
}
