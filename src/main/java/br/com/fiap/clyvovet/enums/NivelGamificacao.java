package br.com.fiap.clyvovet.enums;

/**
 * Níveis de gamificação do tutor na plataforma Clyvo VET.
 * Cada nível desbloqueia benefícios e descontos progressivos.
 */
public enum NivelGamificacao {
    BASICO(0, 99, "Básico", 0),
    CUIDADOR(100, 499, "Cuidador", 5),
    TUTOR_PREMIUM(500, Integer.MAX_VALUE, "Tutor Premium", 15);

    private final int pontosMinimos;
    private final int pontosMaximos;
    private final String descricao;
    private final int descontoPercentual;

    NivelGamificacao(int min, int max, String descricao, int desconto) {
        this.pontosMinimos = min;
        this.pontosMaximos = max;
        this.descricao = descricao;
        this.descontoPercentual = desconto;
    }

    public static NivelGamificacao fromPontos(int pontos) {
        for (NivelGamificacao n : values()) {
            if (pontos >= n.pontosMinimos && pontos <= n.pontosMaximos) {
                return n;
            }
        }
        return BASICO;
    }

    public int getPontosMinimos() { return pontosMinimos; }
    public int getPontosMaximos() { return pontosMaximos; }
    public String getDescricao() { return descricao; }
    public int getDescontoPercentual() { return descontoPercentual; }
}
