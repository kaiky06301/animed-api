package br.com.fiap.clyvovet.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lê o token JWT do cabeçalho Authorization e, sendo válido,
 * autentica o usuário no contexto da requisição.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String cabecalho = request.getHeader("Authorization");

        if (cabecalho == null || !cabecalho.startsWith(PREFIXO)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = cabecalho.substring(PREFIXO.length());

        try {
            String email = jwtService.extrairEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails usuario = userDetailsService.loadUserByUsername(email);

                if (jwtService.tokenValido(token, usuario)) {
                    var autenticacao = new UsernamePasswordAuthenticationToken(
                            usuario, null, usuario.getAuthorities());
                    autenticacao.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(autenticacao);
                }
            }
        } catch (Exception e) {
            // Token inválido ou expirado: segue sem autenticar.
            // O acesso será barrado adiante pelas regras de autorização.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
