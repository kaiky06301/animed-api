package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.HistoricoPontuacaoDTO;
import br.com.fiap.clyvovet.enums.TipoAcaoPontuacao;
import br.com.fiap.clyvovet.service.HistoricoPontuacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/historico-pontuacao")
@RequiredArgsConstructor
@Tag(name = "Histórico de Pontuação",
     description = "Auditoria das ações de gamificação que renderam pontos aos tutores")
public class HistoricoPontuacaoController {

    private final HistoricoPontuacaoService historicoService;

    @GetMapping
    @Operation(summary = "Listar todo o histórico (paginado)")
    public ResponseEntity<Page<HistoricoPontuacaoDTO.Response>> listar(
            @PageableDefault(size = 10, sort = "dataHora") Pageable pageable) {
        return ResponseEntity.ok(historicoService.listar(pageable));
    }

    @GetMapping("/por-tutor/{idTutor}")
    @Operation(summary = "Histórico de pontuação de um tutor específico")
    public ResponseEntity<Page<HistoricoPontuacaoDTO.Response>> listarPorTutor(
            @PathVariable Long idTutor,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(historicoService.listarPorTutor(idTutor, pageable));
    }

    @GetMapping("/por-tipo/{tipo}")
    @Operation(summary = "Filtrar histórico por tipo de ação")
    public ResponseEntity<Page<HistoricoPontuacaoDTO.Response>> porTipo(
            @PathVariable TipoAcaoPontuacao tipo,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(historicoService.listarPorTipo(tipo, pageable));
    }
}
