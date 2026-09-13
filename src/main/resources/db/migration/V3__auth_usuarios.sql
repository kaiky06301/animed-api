-- ============================================================================
-- V3 - Autenticação e controle de acesso (Sprint 3)
--
-- Cria a tabela de credenciais usada pelo Spring Security. O perfil (ROLE)
-- define as permissões: TUTOR gerencia os próprios pets, DOUTOR cadastra
-- tutores e acompanha os pacientes da clínica.
-- ============================================================================

CREATE TABLE TB_USUARIO (
    ID_USUARIO      NUMBER(19)    GENERATED ALWAYS AS IDENTITY,
    EMAIL           VARCHAR2(150) NOT NULL,
    SENHA           VARCHAR2(100) NOT NULL,
    ROLE            VARCHAR2(20)  NOT NULL,
    ID_TUTOR        NUMBER(19),
    ATIVO           NUMBER(1)     DEFAULT 1 NOT NULL,
    DATA_CADASTRO   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_USUARIO        PRIMARY KEY (ID_USUARIO),
    CONSTRAINT UK_USUARIO_EMAIL  UNIQUE (EMAIL),
    CONSTRAINT FK_USUARIO_TUTOR  FOREIGN KEY (ID_TUTOR) REFERENCES TB_TUTOR(ID_TUTOR),
    CONSTRAINT CK_USUARIO_ROLE   CHECK (ROLE IN ('TUTOR','DOUTOR')),
    CONSTRAINT CK_USUARIO_ATIVO  CHECK (ATIVO IN (0,1))
);

COMMENT ON TABLE  TB_USUARIO IS 'Credenciais de acesso a aplicacao';
COMMENT ON COLUMN TB_USUARIO.SENHA IS 'Hash BCrypt da senha - nunca em texto puro';
COMMENT ON COLUMN TB_USUARIO.ROLE  IS 'Perfil de acesso: TUTOR ou DOUTOR';
COMMENT ON COLUMN TB_USUARIO.ID_TUTOR IS 'Vinculo com TB_TUTOR (nulo quando o perfil e DOUTOR)';

-- Nao ha CREATE INDEX em EMAIL: a constraint UK_USUARIO_EMAIL ja cria um
-- indice unico nessa coluna. No Oracle, indexar a mesma lista de colunas
-- outra vez levanta ORA-01408 e derruba a migration.
