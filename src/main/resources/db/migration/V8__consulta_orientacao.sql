-- ============================================================================
-- V8 - Orientacao do veterinario para o tutor
--
-- O preparo do pet deixa de ser um texto padrao do aplicativo: orientar e
-- ato clinico. Quando o veterinario marca um retorno, ele pode registrar
-- aqui a instrucao que o tutor deve seguir ate la.
-- ============================================================================

ALTER TABLE TB_CONSULTA ADD (ORIENTACAO VARCHAR2(500));

COMMENT ON COLUMN TB_CONSULTA.ORIENTACAO IS 'Orientacao escrita pelo veterinario ao tutor';
