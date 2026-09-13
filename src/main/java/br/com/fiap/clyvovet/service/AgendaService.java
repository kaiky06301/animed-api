package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.AgendaDTO;
import br.com.fiap.clyvovet.entity.Consulta;
import br.com.fiap.clyvovet.entity.Pet;
import br.com.fiap.clyvovet.entity.Tutor;
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
import java.util.Comparator;
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

    /**
     * Custo de desmarcar.
     *
     * É maior que os 10 pontos ganhos ao reservar: o horário perdido faz
     * falta à clínica e a outro tutor que poderia tê-lo usado, então
     * desmarcar precisa pesar mais do que marcar.
     */
    private static final int PONTOS_POR_CANCELAMENTO = 30;

    /** Onde o atendimento acontece. */
    private static final String CLINICA = "Clínica Veterinária Animed";
    private static final String ENDERECO = "Av. Paulista, 1000 - São Paulo/SP";

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

    /** Veterinários em atividade na clínica. */
    public List<Usuario> corpoClinico() {
        List<Usuario> veterinarios = usuarioRepository
                .findByRoleAndAtivoTrueOrderByIdAsc(Role.DOUTOR);

        if (veterinarios.isEmpty()) {
            throw new BusinessException(
                    "Nenhum veterinário disponível para atendimento no momento");
        }
        return veterinarios;
    }

    /** Quantos atendimentos o veterinário já tem marcados na data. */
    private long cargaNoDia(Usuario veterinario, LocalDate data) {
        return consultaRepository
                .findAgendaDoVeterinario(veterinario.getId(),
                        data.atStartOfDay(), data.atTime(LocalTime.MAX))
                .stream()
                .filter(c -> c.getStatus() != StatusConsulta.CANCELADA)
                .count();
    }

    /**
     * Veterinário sugerido quando o tutor não tem preferência.
     *
     * Escolhe quem tem menos atendimentos marcados na data: o tutor espera
     * menos e a clínica distribui o dia em vez de sobrecarregar sempre o
     * mesmo profissional. Empate desempata pela ordem de entrada, para que a
     * sugestão seja estável se o tutor voltar à tela.
     */
    public Usuario veterinarioMaisTranquilo(LocalDate data) {
        return corpoClinico().stream()
                .min(Comparator.comparingLong((Usuario v) -> cargaNoDia(v, data))
                        .thenComparing(Usuario::getId))
                .orElseThrow(() -> new BusinessException(
                        "Nenhum veterinário disponível para atendimento no momento"));
    }

    /** Resolve o profissional do atendimento: o escolhido ou o mais tranquilo. */
    private Usuario resolverVeterinario(Long idVeterinario, LocalDate data) {
        if (idVeterinario == null) {
            return veterinarioMaisTranquilo(data);
        }

        return corpoClinico().stream()
                .filter(v -> v.getId().equals(idVeterinario))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "Veterinário não encontrado ou fora de atividade"));
    }

    /**
     * Corpo clínico com a agenda de cada um na data.
     *
     * Alimenta a escolha do tutor: ele vê quantos horários cada profissional
     * ainda tem livres antes de decidir, e qual deles a clínica sugere.
     */
    public AgendaDTO.CorpoClinico veterinariosDisponiveis(LocalDate data) {
        Usuario sugerido = veterinarioMaisTranquilo(data);

        List<AgendaDTO.VeterinarioDisponivel> profissionais = corpoClinico().stream()
                .map(v -> {
                    AgendaDTO.Disponibilidade agenda = disponibilidade(data, v.getId());
                    return new AgendaDTO.VeterinarioDisponivel(
                            v.getId(),
                            v.getNome(),
                            agenda.horarios().size(),
                            (int) cargaNoDia(v, data),
                            v.getId().equals(sugerido.getId()));
                })
                .toList();

        return new AgendaDTO.CorpoClinico(data, profissionais);
    }

    public AgendaDTO.Disponibilidade disponibilidade(LocalDate data) {
        return disponibilidade(data, null);
    }

    public AgendaDTO.Disponibilidade disponibilidade(LocalDate data, Long idVeterinario) {
        Usuario veterinario = resolverVeterinario(idVeterinario, data);
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
        return mes(ano, mes, null);
    }

    public AgendaDTO.MesDisponivel mes(int ano, int mes, Long idVeterinario) {
        LocalDate primeiro = LocalDate.of(ano, mes, 1);
        List<AgendaDTO.DiaDoMes> dias = new ArrayList<>();

        for (LocalDate data = primeiro; data.getMonthValue() == mes; data = data.plusDays(1)) {
            AgendaDTO.Disponibilidade doDia = disponibilidade(data, idVeterinario);
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
     * O tutor não pode ter dois atendimentos no mesmo horário.
     *
     * Com mais de um veterinário na clínica, dois pets do mesmo tutor cabem
     * no mesmo horário em agendas diferentes — mas quem leva os dois é a
     * mesma pessoa, e ela não consegue estar em duas salas ao mesmo tempo.
     */
    private void exigirTutorLivreNoHorario(Tutor tutor, LocalDateTime quando) {
        consultaRepository.findDoTutorNoHorario(tutor.getId(), quando).stream()
                .filter(c -> c.getStatus() == StatusConsulta.AGENDADA)
                .findFirst()
                .ifPresent(c -> {
                    throw new BusinessException(
                            "Você já tem um atendimento neste horário com "
                                    + c.getPet().getNome()
                                    + ". Escolha outro horário para este pet.");
                });
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
        return agendaDoDia(data, null);
    }

    /**
     * Agenda de um dia.
     *
     * Sem idVeterinario responde pelo primeiro profissional da clínica — é o
     * que o veterinário logado recebe quando abre a própria agenda.
     */
    public AgendaDTO.AgendaDoDia agendaDoDia(LocalDate data, Long idVeterinario) {
        Usuario veterinario = idVeterinario == null
                ? veterinarioDaClinica()
                : resolverVeterinario(idVeterinario, data);

        // Os cancelados continuam na lista do veterinário: ele precisa saber
        // que o horário abriu e que aquele paciente não virá.
        List<AgendaDTO.Atendimento> atendimentos = consultaRepository
                .findAgendaDoVeterinario(veterinario.getId(),
                        data.atStartOfDay(), data.atTime(LocalTime.MAX))
                .stream()
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
                disponibilidade(data, veterinario.getId()).horarios().size(), atendimentos);
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
                consulta.getOrientacao());
    }

    /**
     * Cancela um atendimento marcado.
     *
     * Os pontos ganhos ao reservar o horário voltam atrás: o compromisso
     * não vai acontecer, e manter o crédito permitiria marcar e desmarcar
     * indefinidamente só para pontuar. O horário volta a ficar disponível
     * para outros pacientes.
     */
    @Transactional
    public AgendaDTO.AtendimentoCancelado cancelar(Long idConsulta) {
        Consulta consulta = consultaRepository.findById(idConsulta)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Atendimento não encontrado: " + idConsulta));

        if (consulta.getStatus() == StatusConsulta.REALIZADA) {
            throw new BusinessException("Este atendimento já foi realizado");
        }

        if (consulta.getStatus() == StatusConsulta.CANCELADA) {
            throw new BusinessException("Este atendimento já está cancelado");
        }

        if (consulta.getDataHora().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    "O horário já passou. Fale com a clínica para regularizar o atendimento");
        }

        consulta.setStatus(StatusConsulta.CANCELADA);

        gamificacaoService.estornarAcao(
                consulta.getPet().getTutor().getId(),
                TipoAcaoPontuacao.AGENDAMENTO_CONSULTA,
                PONTOS_POR_CANCELAMENTO,
                "Cancelamento: " + consulta.getMotivo() + " - " + consulta.getPet().getNome());

        return new AgendaDTO.AtendimentoCancelado(
                consulta.getId(),
                consulta.getDataHora(),
                consulta.getMotivo(),
                PONTOS_POR_CANCELAMENTO);
    }

    /**
     * Registra que o paciente não compareceu.
     *
     * Os pontos do agendamento voltam atrás: eles premiam o cuidado com o
     * pet, e marcar horário sem aparecer não é cuidado — além de ocupar uma
     * vaga que faria falta a outro tutor.
     */
    @Transactional
    public AgendaDTO.Atendimento registrarFalta(Long idConsulta) {
        Consulta consulta = consultaRepository.findById(idConsulta)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Atendimento não encontrado: " + idConsulta));

        if (consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new BusinessException(
                    "Só um atendimento ainda marcado pode ser dado como não comparecido");
        }

        if (consulta.getDataHora().isAfter(LocalDateTime.now())) {
            throw new BusinessException("O horário deste atendimento ainda não chegou");
        }

        consulta.setStatus(StatusConsulta.NAO_COMPARECEU);

        gamificacaoService.estornarAcao(
                consulta.getPet().getTutor().getId(),
                TipoAcaoPontuacao.AGENDAMENTO_CONSULTA,
                "Não comparecimento: " + consulta.getMotivo()
                        + " - " + consulta.getPet().getNome());

        return new AgendaDTO.Atendimento(
                consulta.getId(),
                consulta.getDataHora().toLocalTime().toString(),
                consulta.getPet().getId(),
                consulta.getPet().getNome(),
                consulta.getPet().getTutor().getNome(),
                consulta.getMotivo(),
                consulta.getStatus().name());
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

        // O que o veterinário escreveu no fechamento fica na ficha do pet
        if (conclusao != null) {
            if (conclusao.diagnostico() != null && !conclusao.diagnostico().isBlank()) {
                consulta.setDiagnostico(conclusao.diagnostico());
            }
            if (conclusao.prescricao() != null && !conclusao.prescricao().isBlank()) {
                consulta.setPrescricao(conclusao.prescricao());
            }
        }

        TipoAcaoPontuacao acao = consulta.getMotivo() != null
                && consulta.getMotivo().toLowerCase().contains("check-up")
                ? TipoAcaoPontuacao.CHECKUP_REALIZADO
                : TipoAcaoPontuacao.REGISTRO_CONSULTA;

        gamificacaoService.registrarAcao(
                consulta.getPet().getTutor().getId(), acao,
                consulta.getMotivo() + " - " + consulta.getPet().getNome());

        LocalDateTime retorno = conclusao == null ? null : conclusao.retorno();

        if (retorno != null) {
            marcarRetorno(consulta, retorno, conclusao.orientacao());
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
    private void marcarRetorno(Consulta origem, LocalDateTime quando, String orientacao) {
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
                .orientacao(orientacao)
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

        // Sem preferência, a clínica indica quem está com o dia mais tranquilo
        Usuario veterinario = resolverVeterinario(pedido.idVeterinario(), data);

        AgendaDTO.Disponibilidade disponivel = disponibilidade(data, veterinario.getId());

        if (!disponivel.atende() || !disponivel.horarios().contains(horario)) {
            throw new BusinessException(
                    "Este horário não está mais disponível com " + veterinario.getNome());
        }

        exigirDiaLivreParaOPet(pet.getId(), pet.getNome(), data);
        exigirTutorLivreNoHorario(pet.getTutor(), pedido.dataHora());

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
                TipoAcaoPontuacao.AGENDAMENTO_CONSULTA.getPontosPadrao()
        );
    }
}
