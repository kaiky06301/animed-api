package br.com.fiap.clyvovet.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

/**
 * Decide para onde o usuário vai depois de entrar.
 *
 * Tutor e veterinário usam o mesmo formulário de login, mas enxergam
 * sistemas diferentes: um acompanha os próprios pets, o outro atende a
 * clínica inteira. Mandar os dois para a mesma página deixaria cada um a um
 * clique de uma tela que não lhe pertence.
 */
public class PainelPorPerfilHandler implements AuthenticationSuccessHandler {

    private static final String PAINEL_DO_VETERINARIO = "/painel/veterinario";
    private static final String PAINEL_DO_TUTOR = "/painel/tutor";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest requisicao,
                                        HttpServletResponse resposta,
                                        Authentication autenticacao)
            throws IOException, ServletException {

        boolean ehVeterinario = autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_DOUTOR"::equals);

        resposta.sendRedirect(ehVeterinario ? PAINEL_DO_VETERINARIO : PAINEL_DO_TUTOR);
    }
}
