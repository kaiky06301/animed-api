package br.com.fiap.clyvovet.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentação da API e o esquema de autenticação que ela usa.
 *
 * Sem declarar o esquema aqui, o Swagger monta a página mas não oferece
 * onde colar o token — e como quase todo endpoint exige JWT, quem abre a
 * documentação só consegue testar o login e leva 401 no resto. Declarando,
 * aparece o botão "Authorize": cola-se o token uma vez e ele acompanha
 * todas as chamadas seguintes.
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA = "bearerAuth";

    @Bean
    public OpenAPI animedOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Animed API")
                        .version("1.0")
                        .description("""
                                API do Animed, solução do squad para o Challenge da Clyvo VET.

                                Para testar os endpoints protegidos: chame POST /api/auth/login,
                                copie o valor de "token" e cole em Authorize, no topo desta página.

                                Contas de demonstração:
                                doutor@animed.com.br / animed123 — veterinário
                                tutor@animed.com.br  / animed123 — tutor
                                """))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA))
                .components(new Components().addSecuritySchemes(ESQUEMA,
                        new SecurityScheme()
                                .name(ESQUEMA)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Cole aqui apenas o token, sem a palavra Bearer.")));
    }
}
