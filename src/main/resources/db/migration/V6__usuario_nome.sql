-- ============================================================================
-- V6 - Nome do usuario (Sprint 3)
--
-- O perfil DOUTOR nao possui vinculo com TB_TUTOR e por isso nao tinha nome
-- para exibicao, aparecendo pelo e-mail nas telas e nos registros clinicos.
-- ============================================================================

ALTER TABLE TB_USUARIO ADD (NOME VARCHAR2(120));

COMMENT ON COLUMN TB_USUARIO.NOME IS 'Nome de exibicao do usuario';

-- Usuarios existentes herdam o nome do tutor vinculado
UPDATE TB_USUARIO u
   SET NOME = (SELECT t.NOME FROM TB_TUTOR t WHERE t.ID_TUTOR = u.ID_TUTOR)
 WHERE u.ID_TUTOR IS NOT NULL;
