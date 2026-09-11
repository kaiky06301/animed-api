package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.MedicamentoDTO;
import br.com.fiap.clyvovet.service.MedicamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Medicamentos prescritos ao pet e as doses dadas pelo tutor. */
@RestController
@RequestMapping("/api/medicamentos")
@RequiredArgsConstructor
@Tag(name = "Medicamentos", description = "Prescrições do veterinário e doses do tutor")
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    @Operation(
            summary = "Prescreve um medicamento",
            description = "Ato do veterinário: define o remédio, o intervalo e a duração."
    )
    @PostMapping
    public ResponseEntity<MedicamentoDTO.Response> prescrever(
            @Valid @RequestBody MedicamentoDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicamentoService.prescrever(request));
    }

    @Operation(summary = "Medicamentos de um pet")
    @GetMapping("/por-pet/{idPet}")
    public ResponseEntity<List<MedicamentoDTO.Response>> porPet(@PathVariable Long idPet) {
        return ResponseEntity.ok(medicamentoService.listarPorPet(idPet));
    }

    @Operation(
            summary = "Registra uma dose dada",
            description = "Ato do tutor. A dose é sempre registrada; os pontos dependem "
                    + "de o intervalo da receita ter sido respeitado."
    )
    @PostMapping("/{id}/doses")
    public ResponseEntity<MedicamentoDTO.DoseRegistrada> registrarDose(
            @PathVariable Long id,
            @RequestBody(required = false) @Valid MedicamentoDTO.DoseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicamentoService.registrarDose(id, request));
    }

    @Operation(summary = "Encerra o tratamento hoje")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> encerrar(@PathVariable Long id) {
        medicamentoService.encerrar(id);
        return ResponseEntity.noContent().build();
    }
}
