package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.PetDTO;
import br.com.fiap.clyvovet.enums.Especie;
import br.com.fiap.clyvovet.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@Tag(name = "Pets", description = "Gestão de pets cadastrados na plataforma")
public class PetController {

    private final PetService petService;

    @PostMapping
    @Operation(summary = "Cadastrar pet", description = "Cadastra novo pet e atribui pontos ao tutor (gamificação)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pet criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    })
    public ResponseEntity<PetDTO.Response> criar(@Valid @RequestBody PetDTO.Request request) {
        PetDTO.Response response = petService.criar(request);
        return ResponseEntity.created(URI.create("/api/pets/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pet por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pet encontrado"),
            @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    public ResponseEntity<PetDTO.Response> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(petService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar pets (paginado)")
    public ResponseEntity<Page<PetDTO.Response>> listar(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(petService.listar(pageable));
    }

    @GetMapping("/por-tutor/{idTutor}")
    @Operation(summary = "Listar pets de um tutor específico")
    public ResponseEntity<Page<PetDTO.Response>> listarPorTutor(
            @PathVariable Long idTutor,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(petService.listarPorTutor(idTutor, pageable));
    }

    @GetMapping("/por-especie/{especie}")
    @Operation(summary = "Filtrar pets por espécie")
    public ResponseEntity<Page<PetDTO.Response>> porEspecie(
            @PathVariable Especie especie,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(petService.buscarPorEspecie(especie, pageable));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar pet por nome (LIKE)")
    public ResponseEntity<Page<PetDTO.Response>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(petService.buscarPorNome(nome, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pet")
    public ResponseEntity<PetDTO.Response> atualizar(@PathVariable Long id, @Valid @RequestBody PetDTO.Request request) {
        return ResponseEntity.ok(petService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar pet")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        petService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
