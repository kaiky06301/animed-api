-- ============================================================================
-- V4 - Sexo do pet (Sprint 3)
--
-- Campo opcional: pets ja cadastrados permanecem sem a informacao ate que
-- o tutor a preencha.
-- ============================================================================

ALTER TABLE TB_PET ADD (SEXO VARCHAR2(10));

ALTER TABLE TB_PET ADD CONSTRAINT CK_PET_SEXO CHECK (SEXO IN ('MACHO','FEMEA'));

COMMENT ON COLUMN TB_PET.SEXO IS 'Sexo do pet: MACHO ou FEMEA (opcional)';
