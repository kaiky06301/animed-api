package br.com.fiap.clyvovet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Clyvo VET API
 *
 * API REST para gestão de cuidado contínuo e gamificação para pets.
 * Desenvolvido para o Challenge FIAP 2026 - Clyvo VET.
 *
 * @author Squad Clyvo VET - FIAP 2TDS Fev/2026
 */
@SpringBootApplication
@EnableCaching
public class ClyvoVetApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClyvoVetApplication.class, args);
    }
}
