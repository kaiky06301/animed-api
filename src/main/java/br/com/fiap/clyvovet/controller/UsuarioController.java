package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.UsuarioDTO;
import br.com.fiap.clyvovet.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contas de acesso do sistema.
 *
 * Área exclusiva do veterinário: é a clínica que decide quem entra e quem
 * deixa de entrar.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Contas de acesso, administradas pela clínica")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(
            summary = "Lista as contas de acesso",
            description = "Ativas primeiro, veterinários antes dos tutores."
    )
    @GetMapping
    public ResponseEntity<List<UsuarioDTO.Response>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @Operation(
            summary = "Liga ou desliga o acesso de uma conta",
            description = "A conta não é apagada: o histórico clínico que a pessoa "
                    + "produziu continua com autoria."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Acesso alterado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio impediu a mudança")
    })
    @PatchMapping("/{id}/acesso")
    public ResponseEntity<UsuarioDTO.Response> mudarAcesso(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioDTO.MudancaDeAcesso mudanca) {
        return ResponseEntity.ok(usuarioService.mudarAcesso(id, mudanca.ativo()));
    }
}
