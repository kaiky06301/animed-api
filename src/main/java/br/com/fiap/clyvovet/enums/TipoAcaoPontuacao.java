package br.com.fiap.clyvovet.enums;

/**
 * Tipos de ação que geram pontos no sistema de gamificação.
 * Cada ação tem um valor padrão de pontos.
 */
public enum TipoAcaoPontuacao {
    CADASTRO_PET(50, "Cadastro completo de pet"),
    PERFIL_COMPLETO(50, "Bônus por perfil completo"),
    REGISTRO_VACINA(25, "Registro de vacinação"),
    REGISTRO_CONSULTA(20, "Registro de consulta veterinária"),
    REGISTRO_MEDICACAO(15, "Registro de medicação"),
    ATUALIZACAO_PESO(5, "Atualização de peso do pet"),
    CHECKUP_REALIZADO(30, "Check-up realizado"),
    VERMIFUGACAO(15, "Vermifugação registrada"),
    AGENDAMENTO_CONSULTA(10, "Consulta agendada"),
    COMPRA_PARCEIRO(20, "Compra em pet shop parceiro"),
    CHECKIN_ESTABELECIMENTO(5, "Check-in em estabelecimento"),
    BONUS_SEMANAL(20, "Bônus por uso semanal"),
    MISSAO_CONCLUIDA(50, "Missão de cuidado concluída");

    private final int pontosPadrao;
    private final String descricao;

    TipoAcaoPontuacao(int pontosPadrao, String descricao) {
        this.pontosPadrao = pontosPadrao;
        this.descricao = descricao;
    }

    public int getPontosPadrao() { return pontosPadrao; }
    public String getDescricao() { return descricao; }
}
