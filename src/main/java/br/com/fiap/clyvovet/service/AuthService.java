package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.auth.AuthResponse;
import br.com.fiap.clyvovet.dto.auth.LoginRequest;
import br.com.fiap.clyvovet.dto.auth.RegistroRequest;
import br.com.fiap.clyvovet.dto.auth.TrocaSenhaRequest;
import br.com.fiap.clyvovet.entity.Tutor;
import br.com.fiap.clyvovet.entity.Usuario;
import br.com.fiap.clyvovet.enums.Role;
import br.com.fiap.clyvovet.exception.BusinessException;
import br.com.fiap.clyvovet.repository.TutorRepository;
import br.com.fiap.clyvovet.repository.UsuarioRepository;
import br.com.fiap.clyvovet.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Regras de autenticação e criação de credenciais.
 *
 * Quando o perfil criado é TUTOR, o registro correspondente em TB_TUTOR
 * é criado junto, de modo que a conta já nasça pronta para gamificação.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final TutorRepository tutorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Troca a senha do usuário autenticado.
     *
     * A senha atual é exigida mesmo com sessão aberta: um aparelho
     * desbloqueado esquecido na mesa não deve permitir tomar a conta.
     */
    @Transactional
    public void trocarSenha(String email, TrocaSenhaRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
            throw new BusinessException("A senha atual está incorreta");
        }

        if (passwordEncoder.matches(request.novaSenha(), usuario.getSenha())) {
            throw new BusinessException("A nova senha precisa ser diferente da atual");
        }

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    /**
     * Decide quem pode criar cada tipo de conta.
     *
     * O cadastro é aberto para que o tutor crie a própria conta — é o que o
     * aplicativo oferece na tela de criar conta. Mas conta de veterinário dá
     * acesso a ato clínico: registrar vacina, prescrever e concluir
     * atendimento. Se o cadastro aberto aceitasse o perfil vindo do corpo da
     * requisição, qualquer pessoa com o endereço da API viraria veterinário da
     * clínica.
     *
     * Por isso: DOUTOR só é criado por quem já é DOUTOR.
     */
    private void exigirPermissaoParaCriar(Role perfilPedido) {
        if (perfilPedido != Role.DOUTOR) {
            return;
        }

        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        boolean ehVeterinarioAutenticado = autenticacao != null
                && autenticacao.isAuthenticated()
                && autenticacao.getAuthorities().stream()
                        .anyMatch(a -> "ROLE_DOUTOR".equals(a.getAuthority()));

        if (!ehVeterinarioAutenticado) {
            throw new BusinessException(
                    "Apenas um veterinário autenticado pode cadastrar outro veterinário");
        }
    }

    public AuthResponse registrar(RegistroRequest request) {

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Já existe uma conta com este e-mail");
        }

        exigirPermissaoParaCriar(request.role());

        Tutor tutor = null;

        if (request.role() == Role.TUTOR) {
            if (tutorRepository.existsByCpf(request.cpf())) {
                throw new BusinessException("Já existe um tutor com este CPF");
            }
            tutor = tutorRepository.save(Tutor.builder()
                    .nome(request.nome())
                    .email(request.email())
                    .cpf(request.cpf())
                    .telefone(request.telefone())
                    .build());
        }

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .role(request.role())
                .tutor(tutor)
                .build());

        return montarResposta(usuario, request.nome());
    }

    @Transactional(readOnly = true)
    public AuthResponse autenticar(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
        } catch (BadCredentialsException e) {
            throw new BusinessException("E-mail ou senha inválidos");
        } catch (DisabledException e) {
            // Acesso desligado pela clínica: a pessoa precisa saber que a conta
            // existe e está suspensa, e não ficar tentando a senha de novo.
            throw new BusinessException(
                    "Este acesso está desativado. Procure a clínica.");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("E-mail ou senha inválidos"));

        // Nome de exibição: o do cadastro do usuário, caindo para o do tutor
        // vinculado e, por último, para o próprio e-mail.
        String nome = usuario.getNome() != null
                ? usuario.getNome()
                : usuario.getTutor() != null
                    ? usuario.getTutor().getNome()
                    : usuario.getEmail();

        return montarResposta(usuario, nome);
    }

    private AuthResponse montarResposta(Usuario usuario, String nome) {
        String token = jwtService.gerarToken(usuario, Map.of("role", usuario.getRole().name()));

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getValidadeMs(),
                usuario.getId(),
                usuario.getTutor() != null ? usuario.getTutor().getId() : null,
                nome,
                usuario.getEmail(),
                usuario.getRole()
        );
    }
}
