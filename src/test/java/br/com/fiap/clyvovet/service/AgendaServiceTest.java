package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.AgendaDTO;
import br.com.fiap.clyvovet.entity.Consulta;
import br.com.fiap.clyvovet.entity.Pet;
import br.com.fiap.clyvovet.entity.Tutor;
import br.com.fiap.clyvovet.enums.StatusConsulta;
import br.com.fiap.clyvovet.enums.TipoAcaoPontuacao;
import br.com.fiap.clyvovet.exception.BusinessException;
import br.com.fiap.clyvovet.repository.ConsultaRepository;
import br.com.fiap.clyvovet.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regras do não comparecimento.
 *
 * Os pontos do agendamento premiam o cuidado com o pet; marcar horário e
 * não aparecer não é cuidado, e ainda tira a vaga de outro tutor. Como a
 * verificação depende de o horário já ter passado, ela é exercitada aqui
 * em vez de esperar o relógio.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AgendaServiceTest {

    @Mock private ConsultaRepository consultaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PetService petService;
    @Mock private GamificacaoService gamificacaoService;

    @InjectMocks private AgendaService service;

    private Consulta consulta;

    @BeforeEach
    void preparar() {
        Tutor tutor = Tutor.builder().id(1L).nome("Tutor").build();
        Pet pet = Pet.builder().id(1L).nome("Thor").tutor(tutor).build();

        consulta = Consulta.builder()
                .id(1L)
                .dataHora(LocalDateTime.now().minusHours(2))
                .motivo("Check-up preventivo")
                .status(StatusConsulta.AGENDADA)
                .pet(pet)
                .build();

        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
    }

    @Test
    @DisplayName("falta registrada estorna os pontos do agendamento")
    void faltaEstornaPontos() {
        AgendaDTO.Atendimento atendimento = service.registrarFalta(1L);

        assertThat(atendimento.status()).isEqualTo("NAO_COMPARECEU");
        assertThat(consulta.getStatus()).isEqualTo(StatusConsulta.NAO_COMPARECEU);

        verify(gamificacaoService).estornarAcao(
                eq(1L), eq(TipoAcaoPontuacao.AGENDAMENTO_CONSULTA), any());
    }

    @Test
    @DisplayName("não dá para marcar falta antes da hora do atendimento")
    void faltaAntesDaHoraEhRecusada() {
        consulta.setDataHora(LocalDateTime.now().plusHours(3));

        assertThatThrownBy(() -> service.registrarFalta(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ainda não chegou");

        verify(gamificacaoService, never()).estornarAcao(anyLong(), any(), any());
    }

    @Test
    @DisplayName("atendimento já realizado não vira falta")
    void realizadoNaoViraFalta() {
        consulta.setStatus(StatusConsulta.REALIZADA);

        assertThatThrownBy(() -> service.registrarFalta(1L))
                .isInstanceOf(BusinessException.class);

        verify(gamificacaoService, never()).estornarAcao(anyLong(), any(), any());
    }

    @Test
    @DisplayName("a falta não é registrada duas vezes")
    void faltaNaoSeRepete() {
        consulta.setStatus(StatusConsulta.NAO_COMPARECEU);

        assertThatThrownBy(() -> service.registrarFalta(1L))
                .isInstanceOf(BusinessException.class);

        verify(gamificacaoService, never()).estornarAcao(anyLong(), any(), any());
    }
}
