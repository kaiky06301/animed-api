-- ============================================================================
-- V5 - Saldo de moedas do tutor (Sprint 3)
--
-- Regra do projeto: cada acao de cuidado credita pontos e moedas no mesmo
-- valor. Os pontos definem o nivel (historico, nunca diminuem); as moedas
-- sao o saldo gastavel, liberado a partir do nivel Premium.
-- ============================================================================

ALTER TABLE TB_TUTOR ADD (MOEDAS NUMBER(10) DEFAULT 0 NOT NULL);

COMMENT ON COLUMN TB_TUTOR.MOEDAS IS 'Saldo de moedas (cashback) - gastavel a partir do nivel Premium';

-- Tutores ja existentes recebem saldo equivalente aos pontos acumulados
UPDATE TB_TUTOR SET MOEDAS = PONTOS_TOTAIS;
