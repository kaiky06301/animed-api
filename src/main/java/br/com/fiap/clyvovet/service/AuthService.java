package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.auth.AuthResponse;
import br.com.fiap.clyvovet.dto.auth.LoginRequest;
import br.com.fiap.clyvovet.dto.auth.RegistroRequest;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Já existe uma conta com este e-mail");
        }

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
