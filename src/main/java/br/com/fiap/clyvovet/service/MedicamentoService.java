package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.MedicamentoDTO;
import br.com.fiap.clyvovet.entity.DoseMedicamento;
import br.com.fiap.clyvovet.entity.Medicamento;
import br.com.fiap.clyvovet.entity.Pet;
import br.com.fiap.clyvovet.entity.Usuario;
import br.com.fiap.clyvovet.enums.Role;
import br.com.fiap.clyvovet.enums.TipoAcaoPontuacao;
import br.com.fiap.clyvovet.exception.BusinessException;
import br.com.fiap.clyvovet.exception.ResourceNotFoundException;
import br.com.fiap.clyvovet.repository.DoseMedicamentoRepository;
import br.com.fiap.clyvovet.repository.MedicamentoRepository;
import br.com.fiap.clyvovet.repository.TutorRepository;
import br.com.fiap.clyvovet.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Medicamentos do pet.
 *
 * Quem prescreve é o veterinário: o tutor não inventa um tratamento, ele
 * cumpre o que foi receitado. Por isso o registro de dose só existe a
 * partir de uma prescrição, e só pontua quando respeita o intervalo
 * indicado na receita.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicamentoService {

    /**
     * Tolerância para o tutor que dá o remédio um pouco antes da hora.
     *
     * Ninguém acerta o minuto exato: vinte minutos de folga evitam punir
     * quem está cumprindo a receita direito.
     */
    private static final int MINUTOS_DE_TOLERANCIA = 20;

    private final MedicamentoRepository medicamentoRepository;
    private final DoseMedicamentoRepository doseRepository;
    private final UsuarioRepository usuarioRepository;
    private final TutorRepository tutorRepository;
    private final PetService petService;
    private final GamificacaoService gamificacaoService;

    @Transactional
    public MedicamentoDTO.Response prescrever(MedicamentoDTO.Request request) {
        Pet pet = petService.buscarEntidade(request.idPet());

        if (request.dataFim() != null && request.dataFim().isBefore(request.dataInicio())) {
            throw new BusinessException("O fim do tratamento não pode ser antes do início");
        }

        Usuario veterinario = usuarioRepository
                .findFirstByRoleAndAtivoTrueOrderByIdAsc(Role.DOUTOR)
                .orElse(null);

        Medicamento medicamento = medicamentoRepository.save(Medicamento.builder()
                .nome(request.nome())
                .dosagem(request.dosagem())
                .intervaloHoras(request.intervaloHoras())
                .dataInicio(request.dataInicio())
                .dataFim(request.dataFim())
                .observacao(request.observacao())
                .pet(pet)
                .veterinario(veterinario)
                .build());

        return toResponse(medicamento);
    }

    public List<MedicamentoDTO.Response> listarPorPet(Long idPet) {
        return medicamentoRepository.findByPet(idPet).stream().map(this::toResponse).toList();
    }

    public Medicamento buscarEntidade(Long id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento", id));
    }

    /**
     * Registra que a dose foi dada.
     *
     * A dose é sempre guardada — o histórico do tratamento tem que refletir
     * a realidade. O que a receita controla é a pontuação: adiantar a dose
     * não rende pontos, e é aí que morre a repetição em série.
     */
    @Transactional
    public MedicamentoDTO.DoseRegistrada registrarDose(Long idMedicamento,
                                                       MedicamentoDTO.DoseRequest request) {
        Medicamento medicamento = buscarEntidade(idMedicamento);
        LocalDateTime agora = LocalDateTime.now();

        if (!medicamento.estaEmCurso(agora.toLocalDate())) {
            throw new BusinessException(medicamento.getDataFim() != null
                    && agora.toLocalDate().isAfter(medicamento.getDataFim())
                    ? "Este tratamento já terminou"
                    : "Este tratamento ainda não começou");
        }

        LocalDateTime liberadaEm = proximaDosePermitida(medicamento);
        boolean noHorario = liberadaEm == null
                || !agora.isBefore(liberadaEm.minusMinutes(MINUTOS_DE_TOLERANCIA));

        DoseMedicamento dose = doseRepository.save(DoseMedicamento.builder()
                .medicamento(medicamento)
                .dataHora(agora)
                .observacao(request == null ? null : request.observacao())
                .build());

        int pontos = 0;

        if (noHorario) {
            gamificacaoService.registrarAcao(
                    medicamento.getPet().getTutor().getId(),
                    TipoAcaoPontuacao.REGISTRO_MEDICACAO,
                    medicamento.getNome() + " - " + medicamento.getPet().getNome());

            pontos = TipoAcaoPontuacao.REGISTRO_MEDICACAO.getPontosPadrao();
        }

        int pontosTotais = tutorRepository.findById(medicamento.getPet().getTutor().getId())
                .map(t -> t.getPontosTotais()).orElse(0);

        return new MedicamentoDTO.DoseRegistrada(
                dose.getId(),
                dose.getDataHora(),
                medicamento.getNome(),
                pontos,
                pontosTotais,
                agora.plusHours(medicamento.getIntervaloHoras()));
    }

    @Transactional
    public void encerrar(Long id) {
        Medicamento medicamento = buscarEntidade(id);
        medicamento.setDataFim(LocalDate.now());
        medicamentoRepository.save(medicamento);
    }

    /** Quando a próxima dose pode ser dada; nulo se nenhuma foi registrada. */
    private LocalDateTime proximaDosePermitida(Medicamento medicamento) {
        return doseRepository
                .findFirstByMedicamentoIdOrderByDataHoraDesc(medicamento.getId())
                .map(d -> d.getDataHora().plusHours(medicamento.getIntervaloHoras()))
                .orElse(null);
    }

    /** "de 12 em 12 horas", "uma vez por dia", "a cada 3 meses". */
    private String posologia(int horas) {
        if (horas < 24) {
            return "de " + horas + " em " + horas + " horas";
        }

        int dias = horas / 24;

        if (dias == 1) return "uma vez por dia";
        if (dias < 30) return "a cada " + dias + " dias";

        int meses = dias / 30;
        return meses == 1 ? "uma vez por mês" : "a cada " + meses + " meses";
    }

    private MedicamentoDTO.Response toResponse(Medicamento m) {
        LocalDateTime ultima = doseRepository
                .findFirstByMedicamentoIdOrderByDataHoraDesc(m.getId())
                .map(DoseMedicamento::getDataHora)
                .orElse(null);

        LocalDateTime proxima = ultima == null ? null : ultima.plusHours(m.getIntervaloHoras());
        boolean emCurso = m.estaEmCurso(LocalDate.now());

        return new MedicamentoDTO.Response(
                m.getId(),
                m.getNome(),
                m.getDosagem(),
                m.getIntervaloHoras(),
                posologia(m.getIntervaloHoras()),
                m.getDataInicio(),
                m.getDataFim(),
                m.getObservacao(),
                m.getVeterinario() == null ? null : m.getVeterinario().getNome(),
                m.getPet().getId(),
                m.getPet().getNome(),
                emCurso,
                doseRepository.countByMedicamentoId(m.getId()),
                ultima,
                proxima,
                emCurso && (proxima == null
                        || !LocalDateTime.now().isBefore(
                                proxima.minusMinutes(MINUTOS_DE_TOLERANCIA))));
    }
}
