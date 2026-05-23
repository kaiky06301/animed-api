package br.com.fiap.clyvovet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test - garante que o contexto Spring sobe.
 * Roda com H2 para não depender de Oracle.
 */
@SpringBootTest
@ActiveProfiles("h2")
class ClyvoVetApplicationTests {

    @Test
    void contextLoads() {
        // Se chegar aqui, o contexto carregou OK
    }
}
