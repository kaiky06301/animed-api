package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.VacinaDTO;
import br.com.fiap.clyvovet.service.VacinaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/vacinas")
@RequiredArgsConstructor
@Tag(name = "Vacinas", description = "Registro de vacinação dos pets")
public class VacinaController {

    private final VacinaService vacinaService;

    @PostMapping
    @Operation(summary = "Registrar vacinação", description = "Cria registro de vacina e gamifica o tutor")
    public ResponseEntity<VacinaDTO.Response> criar(@Valid @RequestBody VacinaDTO.Request request) {
        VacinaDTO.Response response = vacinaService.criar(request);
        return ResponseEntity.created(URI.create("/api/vacinas/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar vacina por ID")
    public ResponseEntity<VacinaDTO.Response> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vacinaService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar vacinas (paginado)")
    public ResponseEntity<Page<VacinaDTO.Response>> listar(
            @PageableDefault(size = 10, sort = "dataAplicacao") Pageable pageable) {
        return ResponseEntity.ok(vacinaService.listar(pageable));
    }

    @GetMapping("/por-pet/{idPet}")
    @Operation(summary = "Listar vacinas de um pet")
    public ResponseEntity<Page<VacinaDTO.Response>> listarPorPet(
            @PathVariable Long idPet,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(vacinaService.listarPorPet(idPet, pageable));
    }

    @GetMapping("/por-tutor/{idTutor}")
    @Operation(summary = "Listar todas as vacinas dos pets de um tutor")
    public ResponseEntity<Page<VacinaDTO.Response>> listarPorTutor(
            @PathVariable Long idTutor,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(vacinaService.listarPorTutor(idTutor, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar registro de vacina")
    public ResponseEntity<VacinaDTO.Response> atualizar(@PathVariable Long id, @Valid @RequestBody VacinaDTO.Request request) {
        return ResponseEntity.ok(vacinaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar vacina")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        vacinaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
