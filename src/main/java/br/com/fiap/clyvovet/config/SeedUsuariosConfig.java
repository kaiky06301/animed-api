package br.com.fiap.clyvovet.config;

import br.com.fiap.clyvovet.entity.Tutor;
import br.com.fiap.clyvovet.entity.Usuario;
import br.com.fiap.clyvovet.enums.Role;
import br.com.fiap.clyvovet.repository.TutorRepository;
import br.com.fiap.clyvovet.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria as contas de demonstração usadas na apresentação do aplicativo.
 *
 * As senhas são gravadas com hash BCrypt gerado em tempo de execução, para que
 * nenhum hash fique versionado no repositório. Executa apenas quando a
 * propriedade animed.seed-usuarios está habilitada.
 */
@Configuration
@ConditionalOnProperty(name = "animed.seed-usuarios", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class SeedUsuariosConfig implements CommandLineRunner {

    private static final String EMAIL_TUTOR  = "tutor@animed.com.br";
    private static final String EMAIL_DOUTOR = "doutor@animed.com.br";
    private static final String SENHA_DEMO   = "animed123";

    private final UsuarioRepository usuarioRepository;
    private final TutorRepository tutorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        criarTutorDemo();
        criarDoutorDemo();
    }

    private void criarTutorDemo() {
        if (usuarioRepository.existsByEmail(EMAIL_TUTOR)) {
            return;
        }

        Tutor tutor = tutorRepository.findByEmail(EMAIL_TUTOR)
                .orElseGet(() -> tutorRepository.save(Tutor.builder()
                        .nome("Tutor Demonstração")
                        .email(EMAIL_TUTOR)
                        .cpf("000.000.000-00")
                        .telefone("(11) 90000-0000")
                        .build()));

        usuarioRepository.save(Usuario.builder()
                .email(EMAIL_TUTOR)
                .senha(passwordEncoder.encode(SENHA_DEMO))
                .role(Role.TUTOR)
                .tutor(tutor)
                .build());

        log.info("Conta de demonstração criada: {} (perfil TUTOR)", EMAIL_TUTOR);
    }

    private void criarDoutorDemo() {
        if (usuarioRepository.existsByEmail(EMAIL_DOUTOR)) {
            return;
        }

        usuarioRepository.save(Usuario.builder()
                .email(EMAIL_DOUTOR)
                .senha(passwordEncoder.encode(SENHA_DEMO))
                .role(Role.DOUTOR)
                .build());

        log.info("Conta de demonstração criada: {} (perfil DOUTOR)", EMAIL_DOUTOR);
    }
}
