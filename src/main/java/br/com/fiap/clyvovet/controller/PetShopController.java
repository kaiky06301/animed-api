package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.PetShopDTO;
import br.com.fiap.clyvovet.service.PetShopService;
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
@RequestMapping("/api/petshops")
@RequiredArgsConstructor
@Tag(name = "Pet Shops", description = "Gestão de parceiros B2B (modelo híbrido: taxa + comissão)")
public class PetShopController {

    private final PetShopService petShopService;

    @PostMapping
    @Operation(summary = "Cadastrar pet shop parceiro")
    public ResponseEntity<PetShopDTO.Response> criar(@Valid @RequestBody PetShopDTO.Request request) {
        PetShopDTO.Response response = petShopService.criar(request);
        return ResponseEntity.created(URI.create("/api/petshops/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pet shop por ID")
    public ResponseEntity<PetShopDTO.Response> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(petShopService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar pet shops")
    public ResponseEntity<Page<PetShopDTO.Response>> listar(
            @PageableDefault(size = 10, sort = "nomeFantasia") Pageable pageable) {
        return ResponseEntity.ok(petShopService.listar(pageable));
    }

    @GetMapping("/ativos")
    @Operation(summary = "Listar apenas pet shops ativos")
    public ResponseEntity<Page<PetShopDTO.Response>> listarAtivos(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(petShopService.listarAtivos(pageable));
    }

    @GetMapping("/por-localizacao")
    @Operation(summary = "Filtrar pet shops por cidade e UF")
    public ResponseEntity<Page<PetShopDTO.Response>> porLocalizacao(
            @RequestParam String cidade,
            @RequestParam String uf,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(petShopService.buscarPorCidadeUf(cidade, uf, pageable));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar pet shop por nome fantasia")
    public ResponseEntity<Page<PetShopDTO.Response>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(petShopService.buscarPorNome(nome, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pet shop")
    public ResponseEntity<PetShopDTO.Response> atualizar(@PathVariable Long id, @Valid @RequestBody PetShopDTO.Request request) {
        return ResponseEntity.ok(petShopService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar pet shop")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        petShopService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
