package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.AgendaDTO;
import br.com.fiap.clyvovet.entity.Consulta;
import br.com.fiap.clyvovet.entity.Pet;
import br.com.fiap.clyvovet.entity.Usuario;
import br.com.fiap.clyvovet.enums.Role;
import br.com.fiap.clyvovet.enums.StatusConsulta;
import br.com.fiap.clyvovet.enums.TipoAcaoPontuacao;
import br.com.fiap.clyvovet.exception.BusinessException;
import br.com.fiap.clyvovet.exception.ResourceNotFoundException;
import br.com.fiap.clyvovet.repository.ConsultaRepository;
import br.com.fiap.clyvovet.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

    /** Onde o atendimento acontece. */
    private static final String CLINICA = "Clínica Veterinária Animed";
    private static final String ENDERECO = "Av. Paulista, 1000 - São Paulo/SP";

    /** Vale para qualquer atendimento. */
    private static final List<String> ORIENTACOES_COMUNS = List.of(
            "Leve a carteira de vacinação e exames anteriores",
            "Traga o pet na caixa de transporte ou com guia e coleira",
            "Chegue com 10 minutos de antecedência"
    );

    /**
     * O check-up costuma incluir coleta de sangue, e é a coleta que pede
     * jejum - não a consulta em si. Por isso o aviso vem condicionado.
     */
    private static final List<String> ORIENTACOES_CHECKUP = List.of(
            "Se houver coleta de sangue, o pet precisa de 8 a 12 horas de jejum "
                    + "- confirme com a clínica ao marcar",
            "Não suspenda a água em nenhuma hipótese"
    );

    /**
     * Diante de um sintoma, jejum por conta própria pode piorar o quadro de
     * um animal já debilitado. O que ajuda a consulta é a informação.
     */
    private static final List<String> ORIENTACOES_SINTOMA = List.of(
            "Não faça jejum sem orientação do veterinário",
            "Anote desde quando o sintoma aparece e o que mudou na rotina",
            "Se houve vômito ou diarreia, leve uma amostra recente"
    );

    /** No retorno, o que importa é a evolução desde o último atendimento. */
    private static final List<String> ORIENTACOES_RETORNO = List.of(
            "Leve os exames e a receita do atendimento anterior",
            "Anote como o pet respondeu ao tratamento"
    );

    private final ConsultaRepository consultaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PetService petService;
    private final GamificacaoService gamificacaoService;

    /**
     * Veterinário dono da agenda.
     *
     * A clínica tem uma profissional responsável pelos atendimentos; é a agenda
     * dela que o tutor enxerga e é nela que o horário é reservado.
     */
    public Usuario veterinarioDaClinica() {
        return usuarioRepository.findFirstByRoleAndAtivoTrueOrderByIdAsc(Role.DOUTOR)
                .orElseThrow(() -> new BusinessException(
                        "Nenhum veterinário disponível para atendimento no momento"));
    }

    public AgendaDTO.Disponibilidade disponibilidade(LocalDate data) {
        Usuario veterinario = veterinarioDaClinica();
        DayOfWeek dia = data.getDayOfWeek();

        if (dia == DayOfWeek.SUNDAY) {
            return indisponivel(data, veterinario, "A clínica não atende aos domingos");
        }

        LocalTime fechamento = dia == DayOfWeek.SATURDAY ? FECHAMENTO_SABADO : FECHAMENTO;

        List<LocalDateTime> ocupados = consultaRepository
                .findAgendaDoVeterinario(veterinario.getId(),
                        data.atStartOfDay(), data.atTime(LocalTime.MAX))
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

        return new AgendaDTO.Disponibilidade(data, true, veterinario.getNome(), observacao,
                livres, MINUTOS_POR_ATENDIMENTO, CLINICA, ENDERECO);
    }

    /**
     * Calendário do mês com os dias que ainda têm horário livre.
     *
     * Alimenta a seleção de data: o tutor enxerga de uma vez em que dias a
     * clínica pode recebê-lo, sem precisar abrir dia a dia.
     */
    public AgendaDTO.MesDisponivel mes(int ano, int mes) {
        LocalDate primeiro = LocalDate.of(ano, mes, 1);
        List<AgendaDTO.DiaDoMes> dias = new ArrayList<>();

        for (LocalDate data = primeiro; data.getMonthValue() == mes; data = data.plusDays(1)) {
            AgendaDTO.Disponibilidade doDia = disponibilidade(data);
            dias.add(new AgendaDTO.DiaDoMes(
                    data, !doDia.horarios().isEmpty(), doDia.horarios().size()));
        }

        return new AgendaDTO.MesDisponivel(ano, mes, dias);
    }

    /**
     * Um pet por dia.
     *
     * Dois atendimentos do mesmo animal no mesmo dia não fazem sentido
     * clínico e só ocupariam a agenda à toa. Pets diferentes, por outro
     * lado, podem ser atendidos no mesmo dia sem qualquer restrição.
     */
    private void exigirDiaLivreParaOPet(Long idPet, String nomeDoPet, LocalDate data) {
        boolean jaTemNoDia = consultaRepository
                .findByPetIdAndDataHoraBetween(idPet,
                        data.atStartOfDay(), data.atTime(LocalTime.MAX))
                .stream()
                .anyMatch(c -> c.getStatus() == StatusConsulta.AGENDADA);

        if (jaTemNoDia) {
            throw new BusinessException(
                    nomeDoPet + " já tem um atendimento marcado neste dia");
        }
    }

    /**
     * Preparo do pet conforme o motivo do atendimento.
     *
     * Orientação genérica é pior que orientação nenhuma: pedir jejum para
     * quem vai a uma consulta de rotina é desnecessário, e pedir a quem
     * leva um animal com sintoma pode agravar o quadro.
     */
    private List<String> orientacoesPara(String motivo) {
        String texto = motivo == null ? "" : motivo.toLowerCase();

        List<String> especificas =
                texto.startsWith("retorno") ? ORIENTACOES_RETORNO
                : texto.contains("sintoma") ? ORIENTACOES_SINTOMA
                : texto.contains("check-up") ? ORIENTACOES_CHECKUP
                : List.of();

        return Stream.concat(especificas.stream(), ORIENTACOES_COMUNS.stream()).toList();
    }

    private AgendaDTO.Disponibilidade indisponivel(LocalDate data, Usuario veterinario,
                                                   String motivo) {
        return new AgendaDTO.Disponibilidade(data, false, veterinario.getNome(), motivo,
                List.of(), MINUTOS_POR_ATENDIMENTO, CLINICA, ENDERECO);
    }

    /**
     * Agenda do dia como o veterinário a enxerga.
     *
     * É a contrapartida da disponibilidade: o que sai da lista de horários
     * livres do tutor aparece aqui como atendimento marcado.
     */
    public AgendaDTO.AgendaDoDia agendaDoDia(LocalDate data) {
        Usuario veterinario = veterinarioDaClinica();

        List<AgendaDTO.Atendimento> atendimentos = consultaRepository
                .findAgendaDoVeterinario(veterinario.getId(),
                        data.atStartOfDay(), data.atTime(LocalTime.MAX))
                .stream()
                .filter(c -> c.getStatus() != StatusConsulta.CANCELADA)
                .map(c -> new AgendaDTO.Atendimento(
                        c.getId(),
                        c.getDataHora().toLocalTime().toString(),
                        c.getPet().getId(),
                        c.getPet().getNome(),
                        c.getPet().getTutor().getNome(),
                        c.getMotivo(),
                        c.getStatus().name()))
                .toList();

        return new AgendaDTO.AgendaDoDia(data, veterinario.getNome(),
                disponibilidade(data).horarios().size(), atendimentos);
    }

    /**
     * Detalhe de um atendimento.
     *
     * Reúne o que está na consulta com o que é da clínica — onde é, quanto
     * dura e como preparar o pet —, para que o tutor não precise procurar
     * essas informações em outro lugar. As orientações de preparo só
     * acompanham atendimentos que ainda vão acontecer.
     */
    public AgendaDTO.DetalheAtendimento detalhe(Long idConsulta) {
        Consulta consulta = consultaRepository.findById(idConsulta)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Atendimento não encontrado: " + idConsulta));

        boolean porVir = consulta.getStatus() == StatusConsulta.AGENDADA;

        return new AgendaDTO.DetalheAtendimento(
                consulta.getId(),
                consulta.getDataHora(),
                consulta.getMotivo(),
                consulta.getStatus().name(),
                consulta.getVeterinario(),
                CLINICA,
                ENDERECO,
                MINUTOS_POR_ATENDIMENTO,
                consulta.getPet().getId(),
                consulta.getPet().getNome(),
                consulta.getDiagnostico(),
                consulta.getPrescricao(),
                porVir ? orientacoesPara(consulta.getMotivo()) : List.of());
    }

    /**
     * Conclui um atendimento da agenda.
     *
     * É o que fecha o ciclo do cuidado: o veterinário confirma que o pet foi
     * atendido e o tutor recebe os pontos prometidos no aplicativo.
     */
    @Transactional
    public AgendaDTO.AtendimentoConcluido concluir(Long idConsulta,
                                                   AgendaDTO.Conclusao conclusao) {
        Consulta consulta = consultaRepository.findById(idConsulta)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Atendimento não encontrado: " + idConsulta));

        if (consulta.getStatus() == StatusConsulta.REALIZADA) {
            throw new BusinessException("Este atendimento já foi concluído");
        }

        if (consulta.getStatus() == StatusConsulta.CANCELADA) {
            throw new BusinessException("Um atendimento cancelado não pode ser concluído");
        }

        consulta.setStatus(StatusConsulta.REALIZADA);

        TipoAcaoPontuacao acao = consulta.getMotivo() != null
                && consulta.getMotivo().toLowerCase().contains("check-up")
                ? TipoAcaoPontuacao.CHECKUP_REALIZADO
                : TipoAcaoPontuacao.REGISTRO_CONSULTA;

        gamificacaoService.registrarAcao(
                consulta.getPet().getTutor().getId(), acao,
                consulta.getMotivo() + " - " + consulta.getPet().getNome());

        LocalDateTime retorno = conclusao == null ? null : conclusao.retorno();

        if (retorno != null) {
            marcarRetorno(consulta, retorno);
        }

        AgendaDTO.Atendimento atendimento = new AgendaDTO.Atendimento(
                consulta.getId(),
                consulta.getDataHora().toLocalTime().toString(),
                consulta.getPet().getId(),
                consulta.getPet().getNome(),
                consulta.getPet().getTutor().getNome(),
                consulta.getMotivo(),
                consulta.getStatus().name());

        return new AgendaDTO.AtendimentoConcluido(atendimento, retorno,
                acao.getPontosPadrao());
    }

    /**
     * Reserva o retorno indicado pelo veterinário.
     *
     * O horário passa pela mesma checagem de disponibilidade do agendamento
     * feito pelo tutor — a agenda é uma só. O retorno não pontua: quem o
     * marcou foi o profissional, não o tutor.
     */
    private void marcarRetorno(Consulta origem, LocalDateTime quando) {
        AgendaDTO.Disponibilidade disponivel = disponibilidade(quando.toLocalDate());
        String horario = quando.toLocalTime().toString();

        if (!disponivel.atende() || !disponivel.horarios().contains(horario)) {
            throw new BusinessException(
                    "A agenda não tem esse horário livre para o retorno");
        }

        exigirDiaLivreParaOPet(origem.getPet().getId(), origem.getPet().getNome(),
                quando.toLocalDate());

        Usuario veterinario = veterinarioDaClinica();

        consultaRepository.save(Consulta.builder()
                .dataHora(quando)
                .motivo("Retorno - " + origem.getMotivo())
                .status(StatusConsulta.AGENDADA)
                .veterinario(veterinario.getNome())
                .veterinarioResponsavel(veterinario)
                .pet(origem.getPet())
                .build());
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

        exigirDiaLivreParaOPet(pet.getId(), pet.getNome(), data);

        Usuario veterinario = veterinarioDaClinica();

        Consulta consulta = consultaRepository.save(Consulta.builder()
                .dataHora(pedido.dataHora())
                .motivo(pedido.motivo())
                .status(StatusConsulta.AGENDADA)
                .veterinario(veterinario.getNome())
                .veterinarioResponsavel(veterinario)
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
                orientacoesPara(consulta.getMotivo())
        );
    }
}
