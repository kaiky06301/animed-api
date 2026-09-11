-- ============================================================================
-- V12 - Moedas usadas como abatimento na compra
--
-- As moedas eram acumuladas e nunca gastas. A transacao passa a registrar
-- quantas foram usadas e quanto isso abateu do valor, para que o saldo do
-- tutor e o valor pago pelo parceiro fechem.
-- ============================================================================

ALTER TABLE TB_TRANSACAO_PARCEIRO ADD (
    MOEDAS_USADAS      NUMBER(10) DEFAULT 0 NOT NULL,
    ABATIMENTO_MOEDAS  NUMBER(10,2) DEFAULT 0 NOT NULL
);

COMMENT ON COLUMN TB_TRANSACAO_PARCEIRO.MOEDAS_USADAS IS 'Moedas gastas pelo tutor nesta compra';
COMMENT ON COLUMN TB_TRANSACAO_PARCEIRO.ABATIMENTO_MOEDAS IS 'Valor em reais abatido pelas moedas';
