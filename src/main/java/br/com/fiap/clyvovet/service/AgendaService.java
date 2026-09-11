package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.AgendaDTO;
import br.com.fiap.clyvovet.entity.Consulta;
import br.com.fiap.clyvovet.entity.Pet;
import br.com.fiap.clyvovet.enums.StatusConsulta;
import br.com.fiap.clyvovet.enums.TipoAcaoPontuacao;
import br.com.fiap.clyvovet.exception.BusinessException;
import br.com.fiap.clyvovet.repository.ConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Agenda de atendimentos da clínica.
 *
 * O expediente e a duração das consultas definem os horários oferecidos ao
 * tutor; horários já ocupados por outros pacientes não são exibidos.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgendaService {

    /** Expediente de segunda a sexta. */
    private static final LocalTime ABERTURA = LocalTime.of(8, 0);
    private static final LocalTime FECHAMENTO = LocalTime.of(18, 0);

    /** Intervalo de almoço, quando a clínica não agenda. */
    private static final LocalTime ALMOCO_INICIO = LocalTime.of(12, 0);
    private static final LocalTime ALMOCO_FIM = LocalTime.of(13, 0);

    /** Sábado tem expediente reduzido. */
    private static final LocalTime FECHAMENTO_SABADO = LocalTime.of(12, 0);

    /** Cada atendimento ocupa este intervalo. */
    private static final int MINUTOS_POR_ATENDIMENTO = 30;

    /** Antecedência mínima para marcar um horário. */
    private static final int HORAS_DE_ANTECEDENCIA = 2;

    private static final List<String> ORIENTACOES = List.of(
            "Mantenha jejum de 8 a 12 horas, salvo orientação diferente do veterinário",
            "Leve a carteira de vacinação e exames anteriores",
            "Traga o pet na caixa de transporte ou com guia e coleira",
            "Chegue com 10 minutos de antecedência"
    );

    private final ConsultaRepository consultaRepository;
    private final PetService petService;
    private final GamificacaoService gamificacaoService;

    public AgendaDTO.Disponibilidade disponibilidade(LocalDate data) {
        DayOfWeek dia = data.getDayOfWeek();

        if (dia == DayOfWeek.SUNDAY) {
            return new AgendaDTO.Disponibilidade(data, false,
                    "A clínica não atende aos domingos", List.of());
        }

        LocalTime fechamento = dia == DayOfWeek.SATURDAY ? FECHAMENTO_SABADO : FECHAMENTO;

        List<LocalDateTime> ocupados = consultaRepository
                .findByDataHoraBetween(data.atStartOfDay(), data.atTime(LocalTime.MAX))
                .stream()
                .filter(c -> c.getStatus() != StatusConsulta.CANCELADA)
                .map(Consulta::getDataHora)
                .toList();

        LocalDateTime limite = LocalDateTime.now().plusHours(HORAS_DE_ANTECEDENCIA);
        List<String> livres = new ArrayList<>();

        for (LocalTime hora = ABERTURA; hora.isBefore(fechamento);
             hora = hora.plusMinutes(MINUTOS_POR_ATENDIMENTO)) {

            // Fora do horário de almoço nos dias de semana
            if (dia != DayOfWeek.SATURDAY
                    && !hora.isBefore(ALMOCO_INICIO) && hora.isBefore(ALMOCO_FIM)) {
                continue;
            }

            LocalDateTime momento = data.atTime(hora);

            if (momento.isBefore(limite)) continue;
            if (ocupados.contains(momento)) continue;

            livres.add(hora.toString());
        }

        String observacao = livres.isEmpty()
                ? "Não há horários livres neste dia"
                : dia == DayOfWeek.SATURDAY ? "Sábado: atendimento das 8h às 12h"
                    : "Atendimento das 8h às 18h, com intervalo entre 12h e 13h";

        return new AgendaDTO.Disponibilidade(data, true, observacao, livres);
    }

    @Transactional
    public AgendaDTO.AgendamentoConfirmado agendar(AgendaDTO.Agendamento pedido) {
        Pet pet = petService.buscarEntidade(pedido.idPet());

        LocalDate data = pedido.dataHora().toLocalDate();
        String horario = pedido.dataHora().toLocalTime().toString();

        AgendaDTO.Disponibilidade disponivel = disponibilidade(data);

        if (!disponivel.atende() || !disponivel.horarios().contains(horario)) {
            throw new BusinessException("Este horário não está mais disponível");
        }

        Consulta consulta = consultaRepository.save(Consulta.builder()
                .dataHora(pedido.dataHora())
                .motivo(pedido.motivo())
                .status(StatusConsulta.AGENDADA)
                .veterinario("Dra. Helena Prado")
                .pet(pet)
                .build());

        gamificacaoService.registrarAcao(
                pet.getTutor().getId(),
                TipoAcaoPontuacao.AGENDAMENTO_CONSULTA,
                "Agendamento: " + pedido.motivo() + " - " + pet.getNome()
        );

        return new AgendaDTO.AgendamentoConfirmado(
                consulta.getId(),
                consulta.getDataHora(),
                consulta.getMotivo(),
                consulta.getVeterinario(),
                TipoAcaoPontuacao.AGENDAMENTO_CONSULTA.getPontosPadrao(),
                ORIENTACOES
        );
    }
}
