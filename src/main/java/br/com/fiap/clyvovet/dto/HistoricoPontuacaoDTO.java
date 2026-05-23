package br.com.fiap.clyvovet.dto;

import br.com.fiap.clyvovet.enums.TipoAcaoPontuacao;

import java.time.LocalDateTime;

public class HistoricoPontuacaoDTO {

    public record Response(
            Long id,
            TipoAcaoPontuacao tipoAcao,
            String tipoAcaoDescricao,
            Integer pontosGanhos,
            String descricao,
            LocalDateTime dataHora,
            Long idTutor
    ) {}
}
