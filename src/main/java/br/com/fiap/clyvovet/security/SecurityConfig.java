package br.com.fiap.clyvovet.security;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuração de autenticação e autorização.
 *
 * A API é stateless: cada requisição carrega o próprio token JWT.
 * As rotas são protegidas conforme o perfil do usuário (TUTOR ou DOUTOR).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UsuarioDetailsService usuarioDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // --- rotas públicas -------------------------------------
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers(
                        "/swagger-ui.html", "/swagger-ui/**",
                        "/v3/api-docs/**", "/h2-console/**").permitAll()

                // --- exclusivas do DOUTOR -------------------------------
                // Só o veterinário cadastra tutores no sistema.
                .requestMatchers(HttpMethod.POST,   "/api/tutores/**").hasRole("DOUTOR")
                .requestMatchers(HttpMethod.DELETE, "/api/tutores/**").hasRole("DOUTOR")
                .requestMatchers("/api/petshops/**").hasRole("DOUTOR")

                // Registro clínico é responsabilidade do veterinário: o tutor
                // acompanha o histórico, mas não o cria nem o altera.
                .requestMatchers(HttpMethod.POST,   "/api/vacinas/**").hasRole("DOUTOR")
                .requestMatchers(HttpMethod.PUT,    "/api/vacinas/**").hasRole("DOUTOR")
                .requestMatchers(HttpMethod.DELETE, "/api/vacinas/**").hasRole("DOUTOR")
                .requestMatchers(HttpMethod.POST,   "/api/consultas/**").hasRole("DOUTOR")
                .requestMatchers(HttpMethod.PUT,    "/api/consultas/**").hasRole("DOUTOR")
                .requestMatchers(HttpMethod.DELETE, "/api/consultas/**").hasRole("DOUTOR")

                // Concluir o atendimento é ato do profissional que atendeu;
                // cancelar é do tutor, e por isso fica fora desta regra.
                .requestMatchers(HttpMethod.PATCH, "/api/agenda/atendimentos/*/concluir")
                    .hasRole("DOUTOR")

                // --- acessíveis aos dois perfis autenticados ------------
                .requestMatchers("/api/pets/**", "/api/vacinas/**",
                                 "/api/consultas/**", "/api/historico-pontuacao/**",
                                 "/api/cuidados/**", "/api/agenda/**")
                    .hasAnyRole("TUTOR", "DOUTOR")

                .anyRequest().authenticated()
            )
            // Sem token válido a resposta é 401 (não autenticado), e não 403.
            .exceptionHandling(ex -> ex.authenticationEntryPoint(
                    (request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(
                                "{\"status\":401,\"error\":\"Unauthorized\","
                                + "\"message\":\"Autenticação necessária\"}");
                    })
                    // Autenticado, porém sem permissão para a rota: 403.
                    .accessDeniedHandler((request, response, deniedException) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(
                                "{\"status\":403,\"error\":\"Forbidden\","
                                + "\"message\":\"Seu perfil não tem permissão para esta operação\"}");
                    }))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(h -> h.frameOptions(f -> f.sameOrigin())); // necessário para o console H2

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /** Libera o consumo pelo aplicativo mobile durante o desenvolvimento. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
