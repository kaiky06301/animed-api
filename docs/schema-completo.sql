-- ============================================================================
-- ANIMED — Schema completo (Oracle)
--
-- Consolidação das migrations V1 a V12 aplicadas pelo Flyway na API Java,
-- na mesma ordem. Gerado para a equipe de .NET modelar o EF Core sobre o
-- mesmo banco, e para a disciplina de Database executar no Oracle FIAP.
--
-- As tabelas guardam os nomes históricos do projeto (CLYVO, COMISSAO_CLYVO):
-- são identificadores funcionais e não devem ser renomeados.
--
-- Para recomeçar do zero, rode antes o bloco de limpeza no fim do arquivo.
-- ============================================================================

-- ============================================================
-- V1__init_schema.sql
-- ============================================================

-- =====================================================
-- Animed - Schema inicial (V1)
-- Banco: Oracle
-- =====================================================

-- =====================================================
-- TB_TUTOR
-- =====================================================
CREATE TABLE TB_TUTOR (
    ID_TUTOR        NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    NOME            VARCHAR2(120) NOT NULL,
    EMAIL           VARCHAR2(150) NOT NULL,
    CPF             VARCHAR2(14)  NOT NULL,
    TELEFONE        VARCHAR2(20),
    PONTOS_TOTAIS   NUMBER(10) DEFAULT 0 NOT NULL,
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

-- =====================================================
-- TB_PET
-- =====================================================
CREATE TABLE TB_PET (
    ID_PET              NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    NOME                VARCHAR2(80) NOT NULL,
    ESPECIE             VARCHAR2(20) NOT NULL,
    RACA                VARCHAR2(80),
    DATA_NASCIMENTO     DATE,
    PESO_KG             NUMBER(5,2),
    CASTRADO            NUMBER(1) DEFAULT 0,
    OBSERVACOES_SAUDE   VARCHAR2(1000),
    ID_TUTOR            NUMBER(19) NOT NULL,
    CONSTRAINT FK_PET_TUTOR FOREIGN KEY (ID_TUTOR) REFERENCES TB_TUTOR(ID_TUTOR),
    CONSTRAINT CK_PET_ESPECIE CHECK (ESPECIE IN ('CACHORRO','GATO','AVE','ROEDOR','REPTIL','OUTRO'))
);

-- =====================================================
-- TB_PETSHOP
-- =====================================================
CREATE TABLE TB_PETSHOP (
    ID_PETSHOP            NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    RAZAO_SOCIAL          VARCHAR2(150) NOT NULL,
    NOME_FANTASIA         VARCHAR2(120) NOT NULL,
    CNPJ                  VARCHAR2(18) NOT NULL,
    ENDERECO              VARCHAR2(250),
    CIDADE                VARCHAR2(100),
    UF                    VARCHAR2(2),
    TELEFONE              VARCHAR2(20),
    TAXA_MENSAL           NUMBER(10,2) DEFAULT 99.90 NOT NULL,
    COMISSAO_PERCENTUAL   NUMBER(5,2) DEFAULT 5.00 NOT NULL,
    ATIVO                 NUMBER(1) DEFAULT 1 NOT NULL,
    DATA_PARCERIA         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT UK_PETSHOP_CNPJ UNIQUE (CNPJ)
);

-- =====================================================
-- TB_VACINA
-- =====================================================
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

-- =====================================================
-- TB_CONSULTA
-- =====================================================
CREATE TABLE TB_CONSULTA (
    ID_CONSULTA   NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    DATA_HORA     TIMESTAMP NOT NULL,
    MOTIVO        VARCHAR2(250) NOT NULL,
    DIAGNOSTICO   VARCHAR2(1000),
    PRESCRICAO    VARCHAR2(1000),
    VALOR         NUMBER(10,2),
    STATUS        VARCHAR2(20) DEFAULT 'AGENDADA' NOT NULL,
    VETERINARIO   VARCHAR2(120),
    ID_PET        NUMBER(19) NOT NULL,
    ID_PETSHOP    NUMBER(19),
    CONSTRAINT FK_CONSULTA_PET FOREIGN KEY (ID_PET) REFERENCES TB_PET(ID_PET),
    CONSTRAINT FK_CONSULTA_PETSHOP FOREIGN KEY (ID_PETSHOP) REFERENCES TB_PETSHOP(ID_PETSHOP),
    CONSTRAINT CK_CONSULTA_STATUS CHECK (STATUS IN ('AGENDADA','REALIZADA','CANCELADA','NAO_COMPARECEU'))
);

-- =====================================================
-- TB_TRANSACAO_PARCEIRO
-- =====================================================
CREATE TABLE TB_TRANSACAO_PARCEIRO (
    ID_TRANSACAO        NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    DATA_HORA           TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    VALOR_BRUTO         NUMBER(10,2) NOT NULL,
    DESCONTO_APLICADO   NUMBER(10,2) DEFAULT 0,
    VALOR_FINAL         NUMBER(10,2) NOT NULL,
    COMISSAO_CLYVO      NUMBER(10,2) NOT NULL,
    PONTOS_GERADOS      NUMBER(10) NOT NULL,
    DESCRICAO_PRODUTO   VARCHAR2(250),
    ID_TUTOR            NUMBER(19) NOT NULL,
    ID_PETSHOP          NUMBER(19) NOT NULL,
    CONSTRAINT FK_TRANSACAO_TUTOR FOREIGN KEY (ID_TUTOR) REFERENCES TB_TUTOR(ID_TUTOR),
    CONSTRAINT FK_TRANSACAO_PETSHOP FOREIGN KEY (ID_PETSHOP) REFERENCES TB_PETSHOP(ID_PETSHOP)
);

-- =====================================================
-- TB_HISTORICO_PONTUACAO
-- =====================================================
CREATE TABLE TB_HISTORICO_PONTUACAO (
    ID_HISTORICO   NUMBER(19) GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    TIPO_ACAO      VARCHAR2(40) NOT NULL,
    PONTOS_GANHOS  NUMBER(10) NOT NULL,
    DESCRICAO      VARCHAR2(250),
    DATA_HORA      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ID_TUTOR       NUMBER(19) NOT NULL,
    CONSTRAINT FK_HIST_TUTOR FOREIGN KEY (ID_TUTOR) REFERENCES TB_TUTOR(ID_TUTOR)
);

-- =====================================================
-- ÍNDICES (performance em buscas comuns)
-- =====================================================
CREATE INDEX IDX_PET_TUTOR ON TB_PET(ID_TUTOR);
CREATE INDEX IDX_VACINA_PET ON TB_VACINA(ID_PET);
CREATE INDEX IDX_VACINA_DATA ON TB_VACINA(DATA_APLICACAO);
CREATE INDEX IDX_CONSULTA_PET ON TB_CONSULTA(ID_PET);
CREATE INDEX IDX_CONSULTA_DATA ON TB_CONSULTA(DATA_HORA);
CREATE INDEX IDX_TRANSACAO_TUTOR ON TB_TRANSACAO_PARCEIRO(ID_TUTOR);
CREATE INDEX IDX_TRANSACAO_PETSHOP ON TB_TRANSACAO_PARCEIRO(ID_PETSHOP);
CREATE INDEX IDX_HIST_TUTOR ON TB_HISTORICO_PONTUACAO(ID_TUTOR);
CREATE INDEX IDX_TUTOR_PONTOS ON TB_TUTOR(PONTOS_TOTAIS DESC);

-- ============================================================
-- V2__seed_data.sql
-- ============================================================

-- =====================================================
-- Animed - Seed de dados iniciais (V2)
-- Dados de demonstração para o squad e correção FIAP
-- =====================================================

-- TUTORES
INSERT INTO TB_TUTOR (NOME, EMAIL, CPF, TELEFONE, PONTOS_TOTAIS, NIVEL, PLANO)
VALUES ('Marina Oliveira', 'marina.oliveira@email.com', '123.456.789-00', '(11) 98765-4321', 350, 'CUIDADOR', 'INTERMEDIARIO');

INSERT INTO TB_TUTOR (NOME, EMAIL, CPF, TELEFONE, PONTOS_TOTAIS, NIVEL, PLANO)
VALUES ('Carlos Mendes', 'carlos.mendes@email.com', '987.654.321-00', '(11) 91234-5678', 720, 'TUTOR_PREMIUM', 'PREMIUM');

INSERT INTO TB_TUTOR (NOME, EMAIL, CPF, TELEFONE, PONTOS_TOTAIS, NIVEL, PLANO)
VALUES ('Juliana Santos', 'juliana.santos@email.com', '456.789.123-00', '(11) 99876-5432', 50, 'BASICO', 'GRATUITO');

-- PET SHOPS PARCEIROS
INSERT INTO TB_PETSHOP (RAZAO_SOCIAL, NOME_FANTASIA, CNPJ, ENDERECO, CIDADE, UF, TELEFONE, TAXA_MENSAL, COMISSAO_PERCENTUAL)
VALUES ('Mundo Pet Comércio Ltda', 'Mundo Pet', '12.345.678/0001-99', 'Av. Paulista, 1500', 'São Paulo', 'SP', '(11) 3333-4444', 99.90, 5.00);

INSERT INTO TB_PETSHOP (RAZAO_SOCIAL, NOME_FANTASIA, CNPJ, ENDERECO, CIDADE, UF, TELEFONE, TAXA_MENSAL, COMISSAO_PERCENTUAL)
VALUES ('Cobasi Animais e Plantas', 'Cobasi Vila Mariana', '98.765.432/0001-11', 'Rua Domingos de Morais, 800', 'São Paulo', 'SP', '(11) 4444-5555', 199.90, 7.00);

-- PETS
INSERT INTO TB_PET (NOME, ESPECIE, RACA, DATA_NASCIMENTO, PESO_KG, CASTRADO, OBSERVACOES_SAUDE, ID_TUTOR)
VALUES ('Thor', 'CACHORRO', 'Golden Retriever', DATE '2020-03-15', 32.5, 1, 'Saudável, sem alergias conhecidas', 1);

INSERT INTO TB_PET (NOME, ESPECIE, RACA, DATA_NASCIMENTO, PESO_KG, CASTRADO, OBSERVACOES_SAUDE, ID_TUTOR)
VALUES ('Mia', 'GATO', 'Persa', DATE '2021-07-20', 4.2, 1, 'Histórico de cistite, dieta especial', 1);

INSERT INTO TB_PET (NOME, ESPECIE, RACA, DATA_NASCIMENTO, PESO_KG, CASTRADO, OBSERVACOES_SAUDE, ID_TUTOR)
VALUES ('Rex', 'CACHORRO', 'Pastor Alemão', DATE '2018-11-10', 38.0, 1, 'Displasia coxofemoral leve', 2);

INSERT INTO TB_PET (NOME, ESPECIE, RACA, DATA_NASCIMENTO, PESO_KG, CASTRADO, OBSERVACOES_SAUDE, ID_TUTOR)
VALUES ('Luna', 'GATO', 'SRD', DATE '2023-01-05', 3.8, 0, 'Filhote adotada de ONG', 3);

-- VACINAS
INSERT INTO TB_VACINA (NOME_VACINA, DATA_APLICACAO, DATA_PROXIMA_DOSE, VETERINARIO_RESPONSAVEL, CLINICA, LOTE, ID_PET)
VALUES ('V10 (Polivalente Canina)', DATE '2024-03-15', DATE '2025-03-15', 'Dra. Patrícia Lima', 'Clínica VetCare', 'BTH2024-589', 1);

INSERT INTO TB_VACINA (NOME_VACINA, DATA_APLICACAO, DATA_PROXIMA_DOSE, VETERINARIO_RESPONSAVEL, CLINICA, LOTE, ID_PET)
VALUES ('Antirrábica', DATE '2024-04-20', DATE '2025-04-20', 'Dr. Roberto Silva', 'Clínica VetCare', 'AR2024-301', 1);

INSERT INTO TB_VACINA (NOME_VACINA, DATA_APLICACAO, DATA_PROXIMA_DOSE, VETERINARIO_RESPONSAVEL, CLINICA, LOTE, ID_PET)
VALUES ('V4 (Quádrupla Felina)', DATE '2024-05-10', DATE '2025-05-10', 'Dra. Patrícia Lima', 'Clínica VetCare', 'V4-2024-122', 2);

-- CONSULTAS
INSERT INTO TB_CONSULTA (DATA_HORA, MOTIVO, DIAGNOSTICO, VALOR, STATUS, VETERINARIO, ID_PET, ID_PETSHOP)
VALUES (TIMESTAMP '2024-09-15 14:30:00', 'Check-up anual', 'Animal saudável, todos parâmetros normais', 280.00, 'REALIZADA', 'Dra. Patrícia Lima', 1, 1);

INSERT INTO TB_CONSULTA (DATA_HORA, MOTIVO, VALOR, STATUS, VETERINARIO, ID_PET, ID_PETSHOP)
VALUES (TIMESTAMP '2025-06-10 10:00:00', 'Consulta de retorno', 180.00, 'AGENDADA', 'Dra. Patrícia Lima', 2, 1);

-- TRANSAÇÕES (compras em pet shops)
INSERT INTO TB_TRANSACAO_PARCEIRO (VALOR_BRUTO, DESCONTO_APLICADO, VALOR_FINAL, COMISSAO_CLYVO, PONTOS_GERADOS, DESCRICAO_PRODUTO, ID_TUTOR, ID_PETSHOP)
VALUES (150.00, 7.50, 142.50, 7.13, 14, 'Ração Premier Golden 15kg', 1, 1);

INSERT INTO TB_TRANSACAO_PARCEIRO (VALOR_BRUTO, DESCONTO_APLICADO, VALOR_FINAL, COMISSAO_CLYVO, PONTOS_GERADOS, DESCRICAO_PRODUTO, ID_TUTOR, ID_PETSHOP)
VALUES (89.90, 13.49, 76.41, 5.35, 7, 'Antipulgas Bravecto', 2, 2);

-- HISTÓRICO DE PONTUAÇÃO
INSERT INTO TB_HISTORICO_PONTUACAO (TIPO_ACAO, PONTOS_GANHOS, DESCRICAO, ID_TUTOR)
VALUES ('CADASTRO_PET', 50, 'Cadastro do pet Thor', 1);

INSERT INTO TB_HISTORICO_PONTUACAO (TIPO_ACAO, PONTOS_GANHOS, DESCRICAO, ID_TUTOR)
VALUES ('PERFIL_COMPLETO', 100, 'Perfil completo do pet Thor', 1);

INSERT INTO TB_HISTORICO_PONTUACAO (TIPO_ACAO, PONTOS_GANHOS, DESCRICAO, ID_TUTOR)
VALUES ('REGISTRO_VACINA', 30, 'Vacinação: V10 - Thor', 1);

INSERT INTO TB_HISTORICO_PONTUACAO (TIPO_ACAO, PONTOS_GANHOS, DESCRICAO, ID_TUTOR)
VALUES ('CHECKUP_REALIZADO', 60, 'Check-up realizado em Thor', 1);

COMMIT;

-- ============================================================
-- V3__auth_usuarios.sql
-- ============================================================

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

CREATE INDEX IDX_USUARIO_EMAIL ON TB_USUARIO(EMAIL);

-- ============================================================
-- V4__pet_sexo.sql
-- ============================================================

-- ============================================================================
-- V4 - Sexo do pet (Sprint 3)
--
-- Campo opcional: pets ja cadastrados permanecem sem a informacao ate que
-- o tutor a preencha.
-- ============================================================================

ALTER TABLE TB_PET ADD (SEXO VARCHAR2(10));

ALTER TABLE TB_PET ADD CONSTRAINT CK_PET_SEXO CHECK (SEXO IN ('MACHO','FEMEA'));

COMMENT ON COLUMN TB_PET.SEXO IS 'Sexo do pet: MACHO ou FEMEA (opcional)';

-- ============================================================
-- V5__tutor_moedas.sql
-- ============================================================

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

-- ============================================================
-- V6__usuario_nome.sql
-- ============================================================

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

-- ============================================================
-- V7__consulta_veterinario.sql
-- ============================================================

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

-- ============================================================
-- V8__consulta_orientacao.sql
-- ============================================================

-- ============================================================================
-- V8 - Orientacao do veterinario para o tutor
--
-- O preparo do pet deixa de ser um texto padrao do aplicativo: orientar e
-- ato clinico. Quando o veterinario marca um retorno, ele pode registrar
-- aqui a instrucao que o tutor deve seguir ate la.
-- ============================================================================

ALTER TABLE TB_CONSULTA ADD (ORIENTACAO VARCHAR2(500));

COMMENT ON COLUMN TB_CONSULTA.ORIENTACAO IS 'Orientacao escrita pelo veterinario ao tutor';

-- ============================================================
-- V9__pet_ultima_pesagem.sql
-- ============================================================

-- ============================================================================
-- V9 - Controle da ultima pesagem pontuada
--
-- O peso rende pontos, e sem controle o tutor poderia registrar o mesmo pet
-- varias vezes no mesmo dia so para pontuar. A data da ultima pesagem que
-- gerou pontos permite exigir um intervalo minimo entre elas.
-- ============================================================================

ALTER TABLE TB_PET ADD (DATA_ULTIMA_PESAGEM DATE);

COMMENT ON COLUMN TB_PET.DATA_ULTIMA_PESAGEM IS 'Data da ultima pesagem que rendeu pontos';

-- ============================================================
-- V10__medicamentos.sql
-- ============================================================

-- ============================================================================
-- V10 - Medicamentos prescritos e doses administradas
--
-- O registro de medicacao era texto livre do tutor, sem vinculo com qualquer
-- prescricao: qualquer clique valia pontos. Agora o medicamento nasce de uma
-- prescricao do veterinario, com nome, intervalo entre doses e ate quando
-- deve ser tomado; ao tutor cabe registrar as doses efetivamente dadas.
--
-- A vermifugacao passa a ser um medicamento como outro qualquer, com
-- intervalo em dias em vez de horas.
-- ============================================================================

CREATE TABLE TB_MEDICAMENTO (
    ID_MEDICAMENTO   NUMBER(19)    GENERATED ALWAYS AS IDENTITY,
    ID_PET           NUMBER(19)    NOT NULL,
    ID_VETERINARIO   NUMBER(19),
    NOME             VARCHAR2(120) NOT NULL,
    DOSAGEM          VARCHAR2(60),
    INTERVALO_HORAS  NUMBER(5)     NOT NULL,
    DATA_INICIO      DATE          NOT NULL,
    DATA_FIM         DATE,
    OBSERVACAO       VARCHAR2(250),
    DATA_CADASTRO    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_MEDICAMENTO           PRIMARY KEY (ID_MEDICAMENTO),
    CONSTRAINT FK_MEDICAMENTO_PET       FOREIGN KEY (ID_PET) REFERENCES TB_PET(ID_PET),
    CONSTRAINT FK_MEDICAMENTO_VET       FOREIGN KEY (ID_VETERINARIO) REFERENCES TB_USUARIO(ID_USUARIO),
    CONSTRAINT CK_MEDICAMENTO_INTERVALO CHECK (INTERVALO_HORAS > 0)
);

COMMENT ON TABLE  TB_MEDICAMENTO IS 'Prescricao de medicamento feita pelo veterinario';
COMMENT ON COLUMN TB_MEDICAMENTO.INTERVALO_HORAS IS 'Horas entre uma dose e a seguinte';
COMMENT ON COLUMN TB_MEDICAMENTO.DATA_FIM IS 'Ultimo dia do tratamento; nulo quando e continuo';

CREATE INDEX IDX_MEDICAMENTO_PET ON TB_MEDICAMENTO(ID_PET);

CREATE TABLE TB_DOSE_MEDICAMENTO (
    ID_DOSE         NUMBER(19) GENERATED ALWAYS AS IDENTITY,
    ID_MEDICAMENTO  NUMBER(19) NOT NULL,
    DATA_HORA       TIMESTAMP  NOT NULL,
    OBSERVACAO      VARCHAR2(250),
    CONSTRAINT PK_DOSE          PRIMARY KEY (ID_DOSE),
    CONSTRAINT FK_DOSE_REMEDIO  FOREIGN KEY (ID_MEDICAMENTO)
        REFERENCES TB_MEDICAMENTO(ID_MEDICAMENTO)
);

COMMENT ON TABLE TB_DOSE_MEDICAMENTO IS 'Dose efetivamente administrada pelo tutor';

CREATE INDEX IDX_DOSE_MEDICAMENTO ON TB_DOSE_MEDICAMENTO(ID_MEDICAMENTO, DATA_HORA);

-- ============================================================
-- V11__medicamento_confirmacao_fim.sql
-- ============================================================

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

-- ============================================================
-- V12__transacao_moedas.sql
-- ============================================================

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

-- ============================================================================
-- LIMPEZA (opcional) — apaga tudo na ordem inversa das dependências
-- ============================================================================
--
-- DROP TABLE TB_DOSE_MEDICAMENTO    CASCADE CONSTRAINTS;
-- DROP TABLE TB_MEDICAMENTO         CASCADE CONSTRAINTS;
-- DROP TABLE TB_TRANSACAO_PARCEIRO  CASCADE CONSTRAINTS;
-- DROP TABLE TB_HISTORICO_PONTUACAO CASCADE CONSTRAINTS;
-- DROP TABLE TB_CONSULTA            CASCADE CONSTRAINTS;
-- DROP TABLE TB_VACINA              CASCADE CONSTRAINTS;
-- DROP TABLE TB_USUARIO             CASCADE CONSTRAINTS;
-- DROP TABLE TB_PET                 CASCADE CONSTRAINTS;
-- DROP TABLE TB_PETSHOP             CASCADE CONSTRAINTS;
-- DROP TABLE TB_TUTOR               CASCADE CONSTRAINTS;
