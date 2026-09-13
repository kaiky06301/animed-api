package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.AgendaDTO;
import br.com.fiap.clyvovet.dto.PetDTO;
import br.com.fiap.clyvovet.entity.Usuario;
import br.com.fiap.clyvovet.repository.UsuarioRepository;
import br.com.fiap.clyvovet.service.AgendaService;
import br.com.fiap.clyvovet.service.PetService;
import br.com.fiap.clyvovet.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * Camada de visualização da aplicação web.
 *
 * As páginas não têm regra própria: pedem aos mesmos services que a API REST
 * usa e apenas escolhem o que mostrar. Uma regra escrita duas vezes é uma
 * regra que vai divergir — e aqui ela vive no service, um lugar só.
 */
@Controller
@RequiredArgsConstructor
public class PainelController {

    private static final int PETS_POR_PAGINA = 20;

    private final PetService petService;
    private final TutorService tutorService;
    private final AgendaService agendaService;
    private final UsuarioRepository usuarioRepository;

    /** Porta de entrada: manda para o login ou para o painel do perfil. */
    @GetMapping("/")
    public String inicio(@AuthenticationPrincipal UserDetails autenticado) {
        if (autenticado == null) {
            return "redirect:/login";
        }

        boolean ehVeterinario = autenticado.getAuthorities().stream()
                .anyMatch(a -> "ROLE_DOUTOR".equals(a.getAuthority()));

        return ehVeterinario ? "redirect:/painel/veterinario" : "redirect:/painel/tutor";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ------------------------------------------------------------------
    // Área do tutor
    // ------------------------------------------------------------------

    /**
     * Painel do tutor: seus pets, sua pontuação e o que está marcado.
     */
    @GetMapping("/painel/tutor")
    public String painelDoTutor(@AuthenticationPrincipal UserDetails autenticado, Model model) {
        Usuario usuario = usuarioLogado(autenticado);

        if (usuario.getTutor() == null) {
            return "redirect:/login?semTutor";
        }

        Long idTutor = usuario.getTutor().getId();

        List<PetDTO.Response> pets = petService
                .listarPorTutor(idTutor, PageRequest.of(0, PETS_POR_PAGINA))
                .getContent();

        model.addAttribute("tutor", tutorService.buscarPorId(idTutor));
        model.addAttribute("pets", pets);
        model.addAttribute("nome", usuario.getNome());

        return "painel-tutor";
    }

    // ------------------------------------------------------------------
    // Área do veterinário
    // ------------------------------------------------------------------

    /**
     * Painel do veterinário: a agenda de um dia, com o que falta atender.
     *
     * A data vem por parâmetro para que o profissional navegue entre os
     * dias sem sair da página.
     */
    @GetMapping("/painel/veterinario")
    public String painelDoVeterinario(
            @RequestParam(required = false) String data,
            @AuthenticationPrincipal UserDetails autenticado,
            Model model) {

        Usuario usuario = usuarioLogado(autenticado);
        LocalDate dia = (data == null || data.isBlank()) ? LocalDate.now() : LocalDate.parse(data);

        AgendaDTO.AgendaDoDia agenda = agendaService.agendaDoDia(dia, usuario.getId());

        long realizados = agenda.atendimentos().stream()
                .filter(a -> "REALIZADA".equals(a.status()))
                .count();

        model.addAttribute("agenda", agenda);
        model.addAttribute("dia", dia);
        model.addAttribute("diaAnterior", dia.minusDays(1));
        model.addAttribute("diaSeguinte", dia.plusDays(1));
        model.addAttribute("hoje", LocalDate.now());
        model.addAttribute("realizados", realizados);
        model.addAttribute("nome", usuario.getNome());

        return "painel-veterinario";
    }

    /** Pacientes da clínica, com busca por nome do pet ou do tutor. */
    @GetMapping("/painel/veterinario/pacientes")
    public String pacientes(@RequestParam(required = false) String busca,
                            @AuthenticationPrincipal UserDetails autenticado,
                            Model model) {

        List<PetDTO.Response> pets = petService
                .listar(PageRequest.of(0, PETS_POR_PAGINA))
                .getContent();

        if (busca != null && !busca.isBlank()) {
            String termo = busca.toLowerCase();
            pets = pets.stream()
                    .filter(p -> p.nome().toLowerCase().contains(termo)
                            || (p.nomeTutor() != null && p.nomeTutor().toLowerCase().contains(termo)))
                    .toList();
        }

        model.addAttribute("pets", pets);
        model.addAttribute("busca", busca);
        model.addAttribute("nome", usuarioLogado(autenticado).getNome());

        return "pacientes";
    }

    /** Traduz o usuário do Spring Security para a entidade do domínio. */
    private Usuario usuarioLogado(UserDetails autenticado) {
        return usuarioRepository.findByEmail(autenticado.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Usuário autenticado não encontrado: " + autenticado.getUsername()));
    }
}
