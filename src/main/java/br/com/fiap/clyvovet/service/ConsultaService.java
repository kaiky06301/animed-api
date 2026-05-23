package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.ConsultaDTO;
import br.com.fiap.clyvovet.entity.Consulta;
import br.com.fiap.clyvovet.entity.Pet;
import br.com.fiap.clyvovet.entity.PetShop;
import br.com.fiap.clyvovet.enums.StatusConsulta;
import br.com.fiap.clyvovet.enums.TipoAcaoPontuacao;
import br.com.fiap.clyvovet.exception.ResourceNotFoundException;
import br.com.fiap.clyvovet.repository.ConsultaRepository;
import br.com.fiap.clyvovet.repository.PetShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PetService petService;
    private final PetShopRepository petShopRepository;
    private final GamificacaoService gamificacaoService;

    @Transactional
    public ConsultaDTO.Response criar(ConsultaDTO.Request request) {
        Pet pet = petService.buscarEntidade(request.idPet());

        PetShop petShop = null;
        if (request.idPetShop() != null) {
            petShop = petShopRepository.findById(request.idPetShop())
                    .orElseThrow(() -> new ResourceNotFoundException("PetShop", request.idPetShop()));
        }

        Consulta consulta = Consulta.builder()
                .dataHora(request.dataHora())
                .motivo(request.motivo())
                .diagnostico(request.diagnostico())
                .prescricao(request.prescricao())
                .valor(request.valor())
                .status(request.status() != null ? request.status() : StatusConsulta.AGENDADA)
                .veterinario(request.veterinario())
                .pet(pet)
                .petShop(petShop)
                .build();

        Consulta salva = consultaRepository.save(consulta);

        // Gamificação: agendar consulta já rende pontos
        gamificacaoService.registrarAcao(
                pet.getTutor().getId(),
                TipoAcaoPontuacao.REGISTRO_CONSULTA,
                "Consulta agendada para " + pet.getNome()
        );

        // Se já está realizada, dobra (check-up confirmado)
        if (StatusConsulta.REALIZADA.equals(consulta.getStatus())) {
            gamificacaoService.registrarAcao(
                    pet.getTutor().getId(),
                    TipoAcaoPontuacao.CHECKUP_REALIZADO,
                    "Check-up realizado em " + pet.getNome()
            );
        }

        return toResponse(salva);
    }

    public ConsultaDTO.Response buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    public Consulta buscarEntidade(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta", id));
    }

    public Page<ConsultaDTO.Response> listar(Pageable pageable) {
        return consultaRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<ConsultaDTO.Response> listarPorPet(Long idPet, Pageable pageable) {
        return consultaRepository.findByPetId(idPet, pageable).map(this::toResponse);
    }

    public Page<ConsultaDTO.Response> listarPorStatus(StatusConsulta status, Pageable pageable) {
        return consultaRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    public Page<ConsultaDTO.Response> listarPorTutor(Long idTutor, Pageable pageable) {
        return consultaRepository.findByTutor(idTutor, pageable).map(this::toResponse);
    }

    @Transactional
    public ConsultaDTO.Response atualizar(Long id, ConsultaDTO.Request request) {
        Consulta consulta = buscarEntidade(id);
        StatusConsulta statusAntigo = consulta.getStatus();

        consulta.setDataHora(request.dataHora());
        consulta.setMotivo(request.motivo());
        consulta.setDiagnostico(request.diagnostico());
        consulta.setPrescricao(request.prescricao());
        consulta.setValor(request.valor());
        if (request.status() != null) consulta.setStatus(request.status());
        consulta.setVeterinario(request.veterinario());

        Consulta atualizada = consultaRepository.save(consulta);

        // Se mudou de AGENDADA → REALIZADA, gamifica check-up
        if (statusAntigo != StatusConsulta.REALIZADA && StatusConsulta.REALIZADA.equals(consulta.getStatus())) {
            gamificacaoService.registrarAcao(
                    consulta.getPet().getTutor().getId(),
                    TipoAcaoPontuacao.CHECKUP_REALIZADO,
                    "Check-up realizado em " + consulta.getPet().getNome()
            );
        }

        return toResponse(atualizada);
    }

    @Transactional
    public void deletar(Long id) {
        if (!consultaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Consulta", id);
        }
        consultaRepository.deleteById(id);
    }

    private ConsultaDTO.Response toResponse(Consulta c) {
        return new ConsultaDTO.Response(
                c.getId(),
                c.getDataHora(),
                c.getMotivo(),
                c.getDiagnostico(),
                c.getPrescricao(),
                c.getValor(),
                c.getStatus(),
                c.getVeterinario(),
                c.getPet().getId(),
                c.getPet().getNome(),
                c.getPetShop() != null ? c.getPetShop().getId() : null,
                c.getPetShop() != null ? c.getPetShop().getNomeFantasia() : null
        );
    }
}
