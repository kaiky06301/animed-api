package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.CuidadoDTO;
import br.com.fiap.clyvovet.service.CuidadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Registro dos cuidados que o tutor realiza com o pet. */
@RestController
@RequestMapping("/api/cuidados")
@RequiredArgsConstructor
@Tag(name = "Cuidados", description = "Cuidados registrados pelo tutor")
public class CuidadoController {

    private final CuidadoService cuidadoService;

    @Operation(
            summary = "Registra um cuidado do tutor",
            description = "Credita os pontos da ação ao tutor do pet. "
                    + "Registros clínicos são lançados pelo veterinário."
    )
    @ApiResponse(responseCode = "201", description = "Cuidado registrado")
    @PostMapping
    public ResponseEntity<CuidadoDTO.Response> registrar(
            @Valid @RequestBody CuidadoDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cuidadoService.registrar(request));
    }
}
