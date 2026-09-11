-- ============================================================================
-- V9 - Controle da ultima pesagem pontuada
--
-- O peso rende pontos, e sem controle o tutor poderia registrar o mesmo pet
-- varias vezes no mesmo dia so para pontuar. A data da ultima pesagem que
-- gerou pontos permite exigir um intervalo minimo entre elas.
-- ============================================================================

ALTER TABLE TB_PET ADD (DATA_ULTIMA_PESAGEM DATE);

COMMENT ON COLUMN TB_PET.DATA_ULTIMA_PESAGEM IS 'Data da ultima pesagem que rendeu pontos';
