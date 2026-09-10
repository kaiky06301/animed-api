package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.auth.AuthResponse;
import br.com.fiap.clyvovet.dto.auth.LoginRequest;
import br.com.fiap.clyvovet.dto.auth.RegistroRequest;
import br.com.fiap.clyvovet.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Endpoints públicos de autenticação consumidos pelo aplicativo. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Cria uma conta e devolve o token já autenticado. */
    @PostMapping("/registrar")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    /** Valida as credenciais e devolve o token de acesso. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.autenticar(request));
    }
}
