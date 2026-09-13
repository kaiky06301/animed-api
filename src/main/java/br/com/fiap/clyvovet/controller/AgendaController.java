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

    @Operation(
            summary = "Horários livres de um dia",
            description = "Sem idVeterinario, responde pela agenda do profissional "
                    + "com o dia mais tranquilo."
    )
    @GetMapping("/disponibilidade")
    public ResponseEntity<AgendaDTO.Disponibilidade> disponibilidade(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) Long idVeterinario) {
        return ResponseEntity.ok(agendaService.disponibilidade(data, idVeterinario));
    }

    @Operation(
            summary = "Veterinários disponíveis na data",
            description = "Lista o corpo clínico com os horários livres de cada um e "
                    + "indica qual a clínica sugere para quem não tem preferência."
    )
    @GetMapping("/veterinarios")
    public ResponseEntity<AgendaDTO.CorpoClinico> veterinarios(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(agendaService.veterinariosDisponiveis(data));
    }

    @Operation(
            summary = "Calendário do mês",
            description = "Marca quais dias do mês ainda têm horário livre para atendimento."
    )
    @GetMapping("/disponibilidade/mes")
    public ResponseEntity<AgendaDTO.MesDisponivel> mes(
            @RequestParam int ano, @RequestParam int mes,
            @RequestParam(required = false) Long idVeterinario) {
        return ResponseEntity.ok(agendaService.mes(ano, mes, idVeterinario));
    }

    @Operation(
            summary = "Agenda do dia do veterinário",
            description = "Atendimentos marcados na agenda da clínica para a data informada."
    )
    @GetMapping("/dia")
    public ResponseEntity<AgendaDTO.AgendaDoDia> agendaDoDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(agendaService.agendaDoDia(data));
    }

    @Operation(
            summary = "Detalhe de um atendimento",
            description = "Data, horário, profissional, endereço da clínica e preparo do pet."
    )
    @GetMapping("/atendimentos/{id}")
    public ResponseEntity<AgendaDTO.DetalheAtendimento> detalhe(@PathVariable Long id) {
        return ResponseEntity.ok(agendaService.detalhe(id));
    }

    @Operation(
            summary = "Registra que o paciente não compareceu",
            description = "Ato do veterinário, depois do horário. Estorna os pontos "
                    + "que o tutor havia ganho ao marcar."
    )
    @PatchMapping("/atendimentos/{id}/falta")
    public ResponseEntity<AgendaDTO.Atendimento> registrarFalta(@PathVariable Long id) {
        return ResponseEntity.ok(agendaService.registrarFalta(id));
    }

    @Operation(
            summary = "Cancela um atendimento",
            description = "Libera o horário e estorna os pontos ganhos ao marcá-lo."
    )
    @PatchMapping("/atendimentos/{id}/cancelar")
    public ResponseEntity<AgendaDTO.AtendimentoCancelado> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(agendaService.cancelar(id));
    }

    @Operation(
            summary = "Conclui um atendimento",
            description = "Marca o atendimento como realizado, credita os pontos ao tutor "
                    + "e, se o veterinário indicar, já reserva o retorno."
    )
    @PatchMapping("/atendimentos/{id}/concluir")
    public ResponseEntity<AgendaDTO.AtendimentoConcluido> concluir(
            @PathVariable Long id,
            @RequestBody(required = false) @Valid AgendaDTO.Conclusao conclusao) {
        return ResponseEntity.ok(agendaService.concluir(id, conclusao));
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
