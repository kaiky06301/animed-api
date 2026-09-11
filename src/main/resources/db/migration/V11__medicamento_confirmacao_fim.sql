-- ============================================================================
-- V11 - Confirmacao do fim do tratamento pelo tutor
--
-- Quando os dias da receita acabam, o tratamento nao some sozinho da tela: o
-- tutor confirma que terminou. So depois dessa confirmacao o medicamento
-- sai da lista, evitando que um tratamento em curso desapareca por conta de
-- uma data mal preenchida.
-- ============================================================================

ALTER TABLE TB_MEDICAMENTO ADD (DATA_CONFIRMACAO_FIM TIMESTAMP);

COMMENT ON COLUMN TB_MEDICAMENTO.DATA_CONFIRMACAO_FIM IS
    'Quando o tutor confirmou o encerramento do tratamento';
