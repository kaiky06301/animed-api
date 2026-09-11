package br.com.fiap.clyvovet.enums;

/**
 * Cuidados que o próprio tutor realiza e registra.
 *
 * Registros clínicos — vacinação, consulta e check-up — não entram aqui:
 * são lançados pelo veterinário na ficha do paciente.
 */
public enum TipoCuidadoTutor {

    MEDICACAO(TipoAcaoPontuacao.REGISTRO_MEDICACAO, "Medicação administrada"),
    VERMIFUGACAO(TipoAcaoPontuacao.VERMIFUGACAO, "Vermifugação aplicada"),
    PESAGEM(TipoAcaoPontuacao.ATUALIZACAO_PESO, "Peso atualizado"),
    AGENDAMENTO(TipoAcaoPontuacao.AGENDAMENTO_CONSULTA, "Consulta agendada");

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
