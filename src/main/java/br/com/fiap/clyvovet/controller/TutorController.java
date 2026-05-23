package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.TutorDTO;
import br.com.fiap.clyvovet.enums.NivelGamificacao;
import br.com.fiap.clyvovet.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/tutores")
@RequiredArgsConstructor
@Tag(name = "Tutores", description = "Gestão de tutores (responsáveis pelo pet) e sistema de gamificação")
public class TutorController {

    private final TutorService tutorService;

    @PostMapping
    @Operation(summary = "Criar novo tutor",
               description = "Cadastra um novo tutor na plataforma Clyvo VET. Inicia com 0 pontos no nível BÁSICO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tutor criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "422", description = "Email ou CPF já cadastrado")
    })
    public ResponseEntity<EntityModel<TutorDTO.Response>> criar(@Valid @RequestBody TutorDTO.Request request) {
        TutorDTO.Response response = tutorService.criar(request);
        URI location = URI.create("/api/tutores/" + response.id());
        return ResponseEntity.created(location).body(toHateoas(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tutor por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tutor encontrado"),
            @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    })
    public ResponseEntity<EntityModel<TutorDTO.Response>> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toHateoas(tutorService.buscarPorId(id)));
    }

    @GetMapping
    @Operation(summary = "Listar tutores (paginado)")
    public ResponseEntity<Page<TutorDTO.Response>> listar(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(tutorService.listar(pageable));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar tutor por nome (LIKE)")
    public ResponseEntity<Page<TutorDTO.Response>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(tutorService.buscarPorNome(nome, pageable));
    }

    @GetMapping("/por-nivel/{nivel}")
    @Operation(summary = "Listar tutores por nível de gamificação")
    public ResponseEntity<Page<TutorDTO.Response>> porNivel(
            @PathVariable NivelGamificacao nivel,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(tutorService.buscarPorNivel(nivel, pageable));
    }

    @GetMapping("/ranking")
    @Operation(summary = "Ranking de tutores por pontuação",
               description = "Retorna tutores ordenados por pontos (gamificação)")
    public ResponseEntity<Page<TutorDTO.Response>> ranking(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(tutorService.ranking(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tutor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado"),
            @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    })
    public ResponseEntity<EntityModel<TutorDTO.Response>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TutorDTO.Request request) {
        return ResponseEntity.ok(toHateoas(tutorService.atualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar tutor")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deletado"),
            @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        tutorService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // === HATEOAS: nível 3 do modelo de maturidade Richardson ===
    private EntityModel<TutorDTO.Response> toHateoas(TutorDTO.Response t) {
        EntityModel<TutorDTO.Response> model = EntityModel.of(t);
        model.add(linkTo(methodOn(TutorController.class).buscarPorId(t.id())).withSelfRel());
        model.add(linkTo(methodOn(TutorController.class).listar(null)).withRel("tutores"));
        model.add(linkTo(methodOn(TutorController.class).ranking(null)).withRel("ranking"));
        model.add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PetController.class)
                .listarPorTutor(t.id(), null)).withRel("pets"));
        return model;
    }
}
