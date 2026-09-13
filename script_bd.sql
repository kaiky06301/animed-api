-- =============================================================================
-- ANIMED — script_bd.sql
-- Banco: Oracle (container na nuvem — Azure Container Instance)
--
-- Núcleo da aplicação: cuidado contínuo e gamificação do pet.
-- Tabelas de cidade/estado/usuário de acesso NÃO são o núcleo.
-- O CRUD da entrega usa TB_TUTOR e TB_PET (1:N, relacionadas).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- TB_TUTOR
-- Tutor responsável pelo pet. Acumula pontos de cuidado e sobe de nível
-- (BASICO → CUIDADOR → TUTOR_PREMIUM), o que libera desconto e moedas.
-- -----------------------------------------------------------------------------
CREATE TABLE TB_TUTOR (
    ID_TUTOR        NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- chave primária
    NOME            VARCHAR2(120) NOT NULL,
    EMAIL           VARCHAR2(150) NOT NULL,
    CPF             VARCHAR2(14)  NOT NULL,
    TELEFONE        VARCHAR2(20),
    PONTOS_TOTAIS   NUMBER(10) DEFAULT 0 NOT NULL,                       -- histórico de pontos
    MOEDAS          NUMBER(10) DEFAULT 0 NOT NULL,                       -- saldo gastável (Premium)
    NIVEL           VARCHAR2(20) DEFAULT 'BASICO' NOT NULL,
    PLANO           VARCHAR2(20) DEFAULT 'GRATUITO' NOT NULL,
    DATA_CADASTRO   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ATIVO           NUMBER(1) DEFAULT 1 NOT NULL,
    CONSTRAINT UK_TUTOR_EMAIL UNIQUE (EMAIL),
    CONSTRAINT UK_TUTOR_CPF UNIQUE (CPF),
    CONSTRAINT CK_TUTOR_NIVEL CHECK (NIVEL IN ('BASICO','CUIDADOR','TUTOR_PREMIUM')),
    CONSTRAINT CK_TUTOR_PLANO CHECK (PLANO IN ('GRATUITO','INTERMEDIARIO','PREMIUM')),
    CONSTRAINT CK_TUTOR_ATIVO CHECK (ATIVO IN (0,1))
);

COMMENT ON TABLE  TB_TUTOR IS 'Tutor do pet — núcleo da gamificação (pontos, nível, moedas)';
COMMENT ON COLUMN TB_TUTOR.PONTOS_TOTAIS IS 'Pontos acumulados; definem o nível e nunca diminuem por gasto';
COMMENT ON COLUMN TB_TUTOR.MOEDAS IS 'Saldo gastável, liberado a partir do nível Tutor Premium';

-- -----------------------------------------------------------------------------
-- TB_PET
-- Animal cadastrado. Relacionada a TB_TUTOR (N pets para 1 tutor).
-- É o centro do histórico clínico (vacina, consulta, medicamento).
-- -----------------------------------------------------------------------------
CREATE TABLE TB_PET (
    ID_PET              NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- chave primária
    NOME                VARCHAR2(80) NOT NULL,
    ESPECIE             VARCHAR2(20) NOT NULL,
    SEXO                VARCHAR2(10),
    RACA                VARCHAR2(80),
    DATA_NASCIMENTO     DATE,
    PESO_KG             NUMBER(5,2),
    CASTRADO            NUMBER(1) DEFAULT 0,
    OBSERVACOES_SAUDE   VARCHAR2(1000),
    DATA_ULTIMA_PESAGEM DATE,
    ID_TUTOR            NUMBER(19) NOT NULL,                                 -- FK para o tutor
    CONSTRAINT FK_PET_TUTOR FOREIGN KEY (ID_TUTOR) REFERENCES TB_TUTOR(ID_TUTOR),
    CONSTRAINT CK_PET_ESPECIE CHECK (ESPECIE IN ('CACHORRO','GATO','AVE','ROEDOR','REPTIL','OUTRO')),
    CONSTRAINT CK_PET_SEXO CHECK (SEXO IN ('MACHO','FEMEA'))
);

COMMENT ON TABLE  TB_PET IS 'Pet do tutor — núcleo clínico da Animed';
COMMENT ON COLUMN TB_PET.ID_TUTOR IS 'Tutor dono do pet (relacionamento 1:N)';

CREATE INDEX IDX_PET_TUTOR ON TB_PET(ID_TUTOR);

-- -----------------------------------------------------------------------------
-- TB_VACINA
-- Vacina aplicada no pet (ato veterinário). Relacionada a TB_PET.
-- -----------------------------------------------------------------------------
CREATE TABLE TB_VACINA (
    ID_VACINA               NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    NOME_VACINA             VARCHAR2(100) NOT NULL,
    DATA_APLICACAO          DATE NOT NULL,
    DATA_PROXIMA_DOSE       DATE,
    VETERINARIO_RESPONSAVEL VARCHAR2(120),
    CLINICA                 VARCHAR2(150),
    LOTE                    VARCHAR2(50),
    OBSERVACOES             VARCHAR2(500),
    ID_PET                  NUMBER(19) NOT NULL,
    CONSTRAINT FK_VACINA_PET FOREIGN KEY (ID_PET) REFERENCES TB_PET(ID_PET)
);

COMMENT ON TABLE TB_VACINA IS 'Registro de vacinação do pet';

-- -----------------------------------------------------------------------------
-- TB_CONSULTA
-- Atendimento veterinário do pet (agendado ou realizado).
-- -----------------------------------------------------------------------------
CREATE TABLE TB_CONSULTA (
    ID_CONSULTA   NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    DATA_HORA     TIMESTAMP NOT NULL,
    MOTIVO        VARCHAR2(250) NOT NULL,
    DIAGNOSTICO   VARCHAR2(1000),
    PRESCRICAO    VARCHAR2(1000),
    ORIENTACAO    VARCHAR2(500),
    VALOR         NUMBER(10,2),
    STATUS        VARCHAR2(20) DEFAULT 'AGENDADA' NOT NULL,
    VETERINARIO   VARCHAR2(120),
    ID_PET        NUMBER(19) NOT NULL,
    CONSTRAINT FK_CONSULTA_PET FOREIGN KEY (ID_PET) REFERENCES TB_PET(ID_PET),
    CONSTRAINT CK_CONSULTA_STATUS CHECK (STATUS IN ('AGENDADA','REALIZADA','CANCELADA','NAO_COMPARECEU'))
);

COMMENT ON TABLE TB_CONSULTA IS 'Consulta veterinária do pet';

-- -----------------------------------------------------------------------------
-- TB_HISTORICO_PONTUACAO
-- Auditoria das ações de cuidado que geraram (ou estornaram) pontos.
-- -----------------------------------------------------------------------------
CREATE TABLE TB_HISTORICO_PONTUACAO (
    ID_HISTORICO   NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    TIPO_ACAO      VARCHAR2(40) NOT NULL,
    PONTOS_GANHOS  NUMBER(10) NOT NULL,
    DESCRICAO      VARCHAR2(250),
    DATA_HORA      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ID_TUTOR       NUMBER(19) NOT NULL,
    CONSTRAINT FK_HIST_TUTOR FOREIGN KEY (ID_TUTOR) REFERENCES TB_TUTOR(ID_TUTOR)
);

COMMENT ON TABLE TB_HISTORICO_PONTUACAO IS 'Histórico de pontos do tutor';

-- =============================================================================
-- Carga mínima significativa (pelo menos 2 linhas em cada tabela-núcleo)
-- =============================================================================

INSERT INTO TB_TUTOR (NOME, EMAIL, CPF, TELEFONE, PONTOS_TOTAIS, MOEDAS, NIVEL, PLANO)
VALUES ('Marina Oliveira', 'marina.oliveira@email.com', '123.456.789-00', '(11) 98765-4321', 350, 350, 'CUIDADOR', 'INTERMEDIARIO');

INSERT INTO TB_TUTOR (NOME, EMAIL, CPF, TELEFONE, PONTOS_TOTAIS, MOEDAS, NIVEL, PLANO)
VALUES ('Carlos Mendes', 'carlos.mendes@email.com', '987.654.321-00', '(11) 91234-5678', 720, 720, 'TUTOR_PREMIUM', 'PREMIUM');

INSERT INTO TB_PET (NOME, ESPECIE, SEXO, RACA, DATA_NASCIMENTO, PESO_KG, CASTRADO, OBSERVACOES_SAUDE, ID_TUTOR)
VALUES ('Thor', 'CACHORRO', 'MACHO', 'Golden Retriever', DATE '2020-03-15', 32.5, 1, 'Saudável, sem alergias conhecidas', 1);

INSERT INTO TB_PET (NOME, ESPECIE, SEXO, RACA, DATA_NASCIMENTO, PESO_KG, CASTRADO, OBSERVACOES_SAUDE, ID_TUTOR)
VALUES ('Mia', 'GATO', 'FEMEA', 'Persa', DATE '2021-07-20', 4.2, 1, 'Histórico de cistite, dieta especial', 1);

INSERT INTO TB_VACINA (NOME_VACINA, DATA_APLICACAO, DATA_PROXIMA_DOSE, VETERINARIO_RESPONSAVEL, CLINICA, LOTE, ID_PET)
VALUES ('V10 (Polivalente Canina)', DATE '2024-03-15', DATE '2025-03-15', 'Dra. Patrícia Lima', 'Clínica VetCare', 'BTH2024-589', 1);

INSERT INTO TB_VACINA (NOME_VACINA, DATA_APLICACAO, DATA_PROXIMA_DOSE, VETERINARIO_RESPONSAVEL, CLINICA, LOTE, ID_PET)
VALUES ('Antirrábica', DATE '2024-04-20', DATE '2025-04-20', 'Dr. Roberto Silva', 'Clínica VetCare', 'AR2024-301', 1);

INSERT INTO TB_CONSULTA (DATA_HORA, MOTIVO, DIAGNOSTICO, VALOR, STATUS, VETERINARIO, ID_PET)
VALUES (TIMESTAMP '2024-09-15 14:30:00', 'Check-up anual', 'Animal saudável', 280.00, 'REALIZADA', 'Dra. Patrícia Lima', 1);

INSERT INTO TB_CONSULTA (DATA_HORA, MOTIVO, VALOR, STATUS, VETERINARIO, ID_PET)
VALUES (TIMESTAMP '2025-06-10 10:00:00', 'Consulta de retorno', 180.00, 'AGENDADA', 'Dra. Patrícia Lima', 2);

INSERT INTO TB_HISTORICO_PONTUACAO (TIPO_ACAO, PONTOS_GANHOS, DESCRICAO, ID_TUTOR)
VALUES ('CADASTRO_PET', 50, 'Cadastro do pet Thor', 1);

INSERT INTO TB_HISTORICO_PONTUACAO (TIPO_ACAO, PONTOS_GANHOS, DESCRICAO, ID_TUTOR)
VALUES ('REGISTRO_VACINA', 25, 'Vacinação V10 — Thor', 1);

COMMIT;

-- =============================================================================
-- Consultas para evidenciar o CRUD no vídeo (rode depois de cada operação)
-- =============================================================================
-- SELECT ID_TUTOR, NOME, EMAIL, NIVEL, PONTOS_TOTAIS FROM TB_TUTOR ORDER BY ID_TUTOR;
-- SELECT ID_PET, NOME, ESPECIE, RACA, PESO_KG, ID_TUTOR FROM TB_PET ORDER BY ID_PET;
-- SELECT t.NOME AS TUTOR, p.NOME AS PET, p.ESPECIE
--   FROM TB_PET p JOIN TB_TUTOR t ON t.ID_TUTOR = p.ID_TUTOR;
