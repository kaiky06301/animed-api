package br.com.fiap.clyvovet.enums;

/**
 * Ações que o tutor registra por conta própria.
 *
 * Abrange o cuidado feito em casa, o agendamento e o check-up preventivo,
 * que o tutor acompanha e informa.
 *
 * A vacinação não entra aqui: é ato privativo do médico-veterinário, e a
 * carteira de vacinação deve ser emitida e atualizada por ele
 * (Resolução CFMV nº 1.321/2020).
 */
public enum TipoCuidadoTutor {

    MEDICACAO(TipoAcaoPontuacao.REGISTRO_MEDICACAO, "Medicação administrada"),
    VERMIFUGACAO(TipoAcaoPontuacao.VERMIFUGACAO, "Vermifugação aplicada"),
    PESAGEM(TipoAcaoPontuacao.ATUALIZACAO_PESO, "Peso atualizado"),
    AGENDAMENTO(TipoAcaoPontuacao.AGENDAMENTO_CONSULTA, "Consulta agendada"),
    CHECKUP(TipoAcaoPontuacao.CHECKUP_REALIZADO, "Check-up preventivo realizado");

    private final TipoAcaoPontuacao acao;
    private final String descricao;

    TipoCuidadoTutor(TipoAcaoPontuacao acao, String descricao) {
        this.acao = acao;
        this.descricao = descricao;
    }

    public TipoAcaoPontuacao getAcao() {
        return acao;
    }

    public String getDescricao() {
        return descricao;
    }
}
