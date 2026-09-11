-- ============================================================================
-- V7 - Vinculo da consulta com o veterinario responsavel
--
-- Ate aqui o nome do veterinario era apenas texto livre. Para que a agenda
-- oferecida ao tutor seja exatamente a agenda do profissional que vai
-- atender, a consulta passa a apontar para a credencial do veterinario.
-- ============================================================================

ALTER TABLE TB_CONSULTA ADD (ID_VETERINARIO NUMBER(19));

ALTER TABLE TB_CONSULTA ADD CONSTRAINT FK_CONSULTA_VETERINARIO
    FOREIGN KEY (ID_VETERINARIO) REFERENCES TB_USUARIO(ID_USUARIO);

CREATE INDEX IDX_CONSULTA_VETERINARIO ON TB_CONSULTA(ID_VETERINARIO, DATA_HORA);

COMMENT ON COLUMN TB_CONSULTA.ID_VETERINARIO IS 'Veterinario responsavel pelo atendimento (TB_USUARIO)';
