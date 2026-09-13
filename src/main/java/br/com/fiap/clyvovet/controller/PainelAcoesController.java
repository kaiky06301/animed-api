package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.AgendaDTO;
import br.com.fiap.clyvovet.dto.auth.RegistroRequest;
import br.com.fiap.clyvovet.enums.Role;
import br.com.fiap.clyvovet.entity.Usuario;
import br.com.fiap.clyvovet.exception.BusinessException;
import br.com.fiap.clyvovet.repository.UsuarioRepository;
import br.com.fiap.clyvovet.service.AgendaService;
import br.com.fiap.clyvovet.service.AuthService;
import br.com.fiap.clyvovet.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Ações das páginas: agendar e fechar atendimento.
 *
 * São os dois fluxos completos do sistema — nenhum deles é um CRUD. Agendar
 * consulta a disponibilidade real, respeita as regras da clínica e pontua o
 * tutor; concluir registra o atendimento, pode receitar retorno e credita os
 * pontos prometidos.
 *
 * Como no restante da aplicação, a regra vive no service: aqui só se recebe
 * o formulário, chama-se quem sabe decidir e devolve-se a resposta ao
 * navegador.
 */
@Controller
@RequiredArgsConstructor
public class PainelAcoesController {

    private static final int PETS_POR_PAGINA = 20;

    private final AgendaService agendaService;
    private final AuthService authService;
    private final PetService petService;
    private final UsuarioRepository usuarioRepository;

    // ------------------------------------------------------------------
    // Fluxo 1 — o tutor marca um atendimento
    // ------------------------------------------------------------------

    /** Formulário de agendamento, já com os horários livres do dia escolhido. */
    @GetMapping("/painel/tutor/agendar")
    public String formularioDeAgendamento(
            @RequestParam(required = false) String data,
            @AuthenticationPrincipal UserDetails autenticado,
            Model model) {

        Usuario usuario = usuarioLogado(autenticado);
        LocalDate dia = (data == null || data.isBlank())
                ? LocalDate.now().plusDays(1)
                : LocalDate.parse(data);

        model.addAttribute("pets", petService
                .listarPorTutor(usuario.getTutor().getId(), PageRequest.of(0, PETS_POR_PAGINA))
                .getContent());
        model.addAttribute("disponibilidade", agendaService.disponibilidade(dia));
        model.addAttribute("veterinarios", agendaService.veterinariosDisponiveis(dia).veterinarios());
        model.addAttribute("dia", dia);
        model.addAttribute("nome", usuario.getNome());

        return "agendar";
    }

    @PostMapping("/painel/tutor/agendar")
    public String agendar(@RequestParam Long idPet,
                          @RequestParam String data,
                          @RequestParam String horario,
                          @RequestParam String motivo,
                          @RequestParam(required = false) Long idVeterinario,
                          RedirectAttributes redirecionamento) {

        LocalDateTime quando = LocalDate.parse(data).atTime(LocalTime.parse(horario));

        try {
            AgendaDTO.AgendamentoConfirmado confirmado = agendaService.agendar(
                    new AgendaDTO.Agendamento(idPet, quando, motivo, idVeterinario));

            redirecionamento.addFlashAttribute("sucesso",
                    "Atendimento marcado com " + confirmado.veterinario()
                            + ". Você ganhou " + confirmado.pontosGanhos() + " pontos.");

            return "redirect:/painel/tutor";

        } catch (BusinessException excecao) {
            // Regra de negócio recusou: o tutor precisa saber o motivo
            redirecionamento.addFlashAttribute("erro", excecao.getMessage());
            return "redirect:/painel/tutor/agendar?data=" + data;
        }
    }

    // ------------------------------------------------------------------
    // Fluxo 2 — o veterinário fecha o atendimento
    // ------------------------------------------------------------------

    /** Formulário de conclusão do atendimento. */
    @GetMapping("/painel/veterinario/atendimentos/{id}/concluir")
    public String formularioDeConclusao(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails autenticado,
                                        Model model) {

        model.addAttribute("atendimento", agendaService.detalhe(id));
        model.addAttribute("nome", usuarioLogado(autenticado).getNome());

        return "concluir";
    }

    @PostMapping("/painel/veterinario/atendimentos/{id}/concluir")
    public String concluir(@PathVariable Long id,
                           @RequestParam String diagnostico,
                           @RequestParam(required = false) String prescricao,
                           @RequestParam(required = false) String orientacao,
                           @RequestParam(required = false) String dataRetorno,
                           @RequestParam(required = false) String horarioRetorno,
                           RedirectAttributes redirecionamento) {

        // Retorno só existe quando o profissional preencheu data e hora
        LocalDateTime retorno = (dataRetorno != null && !dataRetorno.isBlank()
                && horarioRetorno != null && !horarioRetorno.isBlank())
                ? LocalDate.parse(dataRetorno).atTime(LocalTime.parse(horarioRetorno))
                : null;

        try {
            AgendaDTO.AtendimentoConcluido concluido = agendaService.concluir(id,
                    new AgendaDTO.Conclusao(retorno, orientacao, diagnostico, prescricao));

            String mensagem = "Atendimento concluído. O tutor ganhou "
                    + concluido.pontosCreditados() + " pontos.";

            if (concluido.retorno() != null) {
                mensagem += " Retorno marcado.";
            }

            redirecionamento.addFlashAttribute("sucesso", mensagem);

        } catch (BusinessException excecao) {
            redirecionamento.addFlashAttribute("erro", excecao.getMessage());
        }

        return "redirect:/painel/veterinario";
    }

    /**
     * Registra a falta do paciente.
     *
     * Os pontos que o tutor ganhou ao agendar voltam atrás: marcar horário e
     * não aparecer tira a vaga de quem precisava dela.
     */
    @PostMapping("/painel/veterinario/atendimentos/{id}/falta")
    public String registrarFalta(@PathVariable Long id, RedirectAttributes redirecionamento) {
        try {
            AgendaDTO.Atendimento atendimento = agendaService.registrarFalta(id);
            redirecionamento.addFlashAttribute("sucesso",
                    atendimento.nomePet() + " não compareceu. Os pontos foram estornados.");

        } catch (BusinessException excecao) {
            redirecionamento.addFlashAttribute("erro", excecao.getMessage());
        }

        return "redirect:/painel/veterinario";
    }

    // ------------------------------------------------------------------
    // Cadastro de pessoas, feito pela clínica
    // ------------------------------------------------------------------

    /** Formulário de cadastro de tutor ou de veterinário. */
    @GetMapping("/painel/veterinario/cadastrar")
    public String formularioDeCadastro(@RequestParam(defaultValue = "TUTOR") String perfil,
                                       @AuthenticationPrincipal UserDetails autenticado,
                                       Model model) {
        model.addAttribute("perfil", perfil);
        model.addAttribute("nome", usuarioLogado(autenticado).getNome());
        return "cadastrar-pessoa";
    }

    /**
     * Cria a conta de um tutor ou de outro veterinário.
     *
     * Quem cadastra é a clínica: o tutor chega ao balcão, e é a recepção que
     * abre o acesso dele. Cadastrar veterinário exige estar autenticado como
     * veterinário — a regra vive no AuthService e vale também para a API.
     */
    @PostMapping("/painel/veterinario/cadastrar")
    public String cadastrar(@RequestParam String nome,
                            @RequestParam String email,
                            @RequestParam String senha,
                            @RequestParam String cpf,
                            @RequestParam(required = false) String telefone,
                            @RequestParam String perfil,
                            RedirectAttributes redirecionamento) {
        try {
            Role papel = Role.valueOf(perfil);

            authService.registrar(new RegistroRequest(
                    nome, email, senha, cpf, telefone, papel));

            String tipo = papel == Role.DOUTOR ? "Veterinário" : "Tutor";
            redirecionamento.addFlashAttribute("sucesso",
                    tipo + " " + nome + " cadastrado. O acesso já está liberado.");

            return "redirect:/painel/veterinario/pacientes";

        } catch (BusinessException excecao) {
            redirecionamento.addFlashAttribute("erro", excecao.getMessage());
            return "redirect:/painel/veterinario/cadastrar?perfil=" + perfil;
        }
    }

    private Usuario usuarioLogado(UserDetails autenticado) {
        return usuarioRepository.findByEmail(autenticado.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Usuário autenticado não encontrado: " + autenticado.getUsername()));
    }
}
