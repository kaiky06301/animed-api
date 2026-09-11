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
