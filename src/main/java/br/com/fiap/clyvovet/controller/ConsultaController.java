package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.ConsultaDTO;
import br.com.fiap.clyvovet.enums.StatusConsulta;
import br.com.fiap.clyvovet.service.ConsultaService;
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
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
@Tag(name = "Consultas", description = "Agendamento e registro de consultas veterinárias")
public class ConsultaController {

    private final ConsultaService consultaService;

    @PostMapping
    @Operation(summary = "Agendar/registrar consulta")
    public ResponseEntity<ConsultaDTO.Response> criar(@Valid @RequestBody ConsultaDTO.Request request) {
        ConsultaDTO.Response response = consultaService.criar(request);
        return ResponseEntity.created(URI.create("/api/consultas/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar consulta por ID")
    public ResponseEntity<ConsultaDTO.Response> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar consultas (paginado)")
    public ResponseEntity<Page<ConsultaDTO.Response>> listar(
            @PageableDefault(size = 10, sort = "dataHora") Pageable pageable) {
        return ResponseEntity.ok(consultaService.listar(pageable));
    }

    @GetMapping("/por-pet/{idPet}")
    @Operation(summary = "Listar consultas de um pet")
    public ResponseEntity<Page<ConsultaDTO.Response>> listarPorPet(
            @PathVariable Long idPet,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(consultaService.listarPorPet(idPet, pageable));
    }

    @GetMapping("/por-tutor/{idTutor}")
    @Operation(summary = "Listar todas consultas dos pets de um tutor")
    public ResponseEntity<Page<ConsultaDTO.Response>> listarPorTutor(
            @PathVariable Long idTutor,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(consultaService.listarPorTutor(idTutor, pageable));
    }

    @GetMapping("/por-status/{status}")
    @Operation(summary = "Filtrar consultas por status")
    public ResponseEntity<Page<ConsultaDTO.Response>> porStatus(
            @PathVariable StatusConsulta status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(consultaService.listarPorStatus(status, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar consulta",
               description = "Mudar status para REALIZADA dispara pontos de check-up automaticamente")
    public ResponseEntity<ConsultaDTO.Response> atualizar(@PathVariable Long id, @Valid @RequestBody ConsultaDTO.Request request) {
        return ResponseEntity.ok(consultaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar consulta")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        consultaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
