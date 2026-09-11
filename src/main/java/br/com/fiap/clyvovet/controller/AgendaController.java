package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.AgendaDTO;
import br.com.fiap.clyvovet.service.AgendaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/** Agenda de atendimentos: horários livres e marcação pelo tutor. */
@RestController
@RequestMapping("/api/agenda")
@RequiredArgsConstructor
@Tag(name = "Agenda", description = "Disponibilidade e agendamento de atendimentos")
public class AgendaController {

    private final AgendaService agendaService;

    @Operation(summary = "Horários livres de um dia")
    @GetMapping("/disponibilidade")
    public ResponseEntity<AgendaDTO.Disponibilidade> disponibilidade(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(agendaService.disponibilidade(data));
    }

    @Operation(
            summary = "Calendário do mês",
            description = "Marca quais dias do mês ainda têm horário livre para atendimento."
    )
    @GetMapping("/disponibilidade/mes")
    public ResponseEntity<AgendaDTO.MesDisponivel> mes(
            @RequestParam int ano, @RequestParam int mes) {
        return ResponseEntity.ok(agendaService.mes(ano, mes));
    }

    @Operation(
            summary = "Agenda um atendimento",
            description = "Marca o horário escolhido pelo tutor e credita os pontos do agendamento."
    )
    @PostMapping("/agendamentos")
    public ResponseEntity<AgendaDTO.AgendamentoConfirmado> agendar(
            @Valid @RequestBody AgendaDTO.Agendamento pedido) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendaService.agendar(pedido));
    }
}
