package br.com.fiap.clyvovet.enums;

/**
 * Ações que o tutor registra por conta própria.
 *
 * Abrange o cuidado feito em casa e a solicitação de atendimento — quem
 * paga pode pedir consulta ou check-up. O resultado clínico, porém, é
 * lançado pelo veterinário: a vacinação, em especial, é ato privativo do
 * médico-veterinário, e a carteira deve ser preenchida por ele
 * (Resolução CFMV nº 1.321/2020).
 */
public enum TipoCuidadoTutor {

    MEDICACAO(TipoAcaoPontuacao.REGISTRO_MEDICACAO, "Medicação administrada"),
    VERMIFUGACAO(TipoAcaoPontuacao.VERMIFUGACAO, "Vermifugação aplicada"),
    PESAGEM(TipoAcaoPontuacao.ATUALIZACAO_PESO, "Peso atualizado"),
    AGENDAMENTO(TipoAcaoPontuacao.AGENDAMENTO_CONSULTA, "Consulta agendada"),
    SOLICITACAO_CHECKUP(TipoAcaoPontuacao.AGENDAMENTO_CONSULTA, "Check-up preventivo solicitado");

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
