package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.TransacaoParceiroDTO;
import br.com.fiap.clyvovet.service.TransacaoParceiroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
@Tag(name = "Transações de Parceiros",
     description = "Compras em pet shops parceiros - aplica desconto por nível e gera comissão Clyvo")
public class TransacaoParceiroController {

    private final TransacaoParceiroService transacaoService;

    @PostMapping
    @Operation(summary = "Registrar compra em pet shop parceiro",
               description = "Calcula automaticamente desconto baseado no nível do tutor, " +
                             "comissão Clyvo (5% sobre valor final) e pontos gerados (1 ponto a cada R$10)")
    public ResponseEntity<TransacaoParceiroDTO.Response> criar(@Valid @RequestBody TransacaoParceiroDTO.Request request) {
        TransacaoParceiroDTO.Response response = transacaoService.criar(request);
        return ResponseEntity.created(URI.create("/api/transacoes/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar transação por ID")
    public ResponseEntity<TransacaoParceiroDTO.Response> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(transacaoService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar todas as transações (paginado)")
    public ResponseEntity<Page<TransacaoParceiroDTO.Response>> listar(
            @PageableDefault(size = 10, sort = "dataHora") Pageable pageable) {
        return ResponseEntity.ok(transacaoService.listar(pageable));
    }

    @GetMapping("/por-tutor/{idTutor}")
    @Operation(summary = "Listar transações de um tutor")
    public ResponseEntity<Page<TransacaoParceiroDTO.Response>> listarPorTutor(
            @PathVariable Long idTutor,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(transacaoService.listarPorTutor(idTutor, pageable));
    }

    @GetMapping("/por-petshop/{idPetShop}")
    @Operation(summary = "Listar transações de um pet shop")
    public ResponseEntity<Page<TransacaoParceiroDTO.Response>> listarPorPetShop(
            @PathVariable Long idPetShop,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(transacaoService.listarPorPetShop(idPetShop, pageable));
    }

    @GetMapping("/relatorios/comissao-petshop/{idPetShop}")
    @Operation(summary = "Total de comissão Clyvo recebida de um pet shop específico")
    public ResponseEntity<Map<String, BigDecimal>> totalComissao(@PathVariable Long idPetShop) {
        BigDecimal total = transacaoService.totalComissaoPorPetShop(idPetShop);
        return ResponseEntity.ok(Map.of("totalComissao", total));
    }

    @GetMapping("/relatorios/gasto-tutor/{idTutor}")
    @Operation(summary = "Total gasto por um tutor em pet shops parceiros")
    public ResponseEntity<Map<String, BigDecimal>> totalGasto(@PathVariable Long idTutor) {
        BigDecimal total = transacaoService.totalGastoPorTutor(idTutor);
        return ResponseEntity.ok(Map.of("totalGasto", total));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Estornar/deletar transação")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        transacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
