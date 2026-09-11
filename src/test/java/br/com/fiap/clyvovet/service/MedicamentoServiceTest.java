package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.MedicamentoDTO;
import br.com.fiap.clyvovet.entity.DoseMedicamento;
import br.com.fiap.clyvovet.entity.Medicamento;
import br.com.fiap.clyvovet.entity.Pet;
import br.com.fiap.clyvovet.entity.Tutor;
import br.com.fiap.clyvovet.repository.DoseMedicamentoRepository;
import br.com.fiap.clyvovet.repository.MedicamentoRepository;
import br.com.fiap.clyvovet.repository.TutorRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regras de pontuação das doses.
 *
 * O intervalo da receita é o que separa o tutor que cumpre o tratamento de
 * quem só repete o registro para pontuar, então cada situação de horário é
 * verificada aqui — inclusive as que levariam horas para reproduzir à mão.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MedicamentoServiceTest {

    @Mock private MedicamentoRepository medicamentoRepository;
    @Mock private DoseMedicamentoRepository doseRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TutorRepository tutorRepository;
    @Mock private PetService petService;
    @Mock private GamificacaoService gamificacaoService;

    @InjectMocks private MedicamentoService service;

    private Medicamento remedio;

    @BeforeEach
    void preparar() {
        Tutor tutor = Tutor.builder().id(1L).nome("Tutor").build();
        Pet pet = Pet.builder().id(1L).nome("Thor").tutor(tutor).build();

        remedio = Medicamento.builder()
                .id(1L)
                .nome("Amoxicilina")
                .intervaloHoras(12)
                .dataInicio(LocalDate.now().minusDays(1))
                .dataFim(LocalDate.now().plusDays(5))
                .pet(pet)
                .build();

        when(medicamentoRepository.findById(1L)).thenReturn(Optional.of(remedio));
        when(doseRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(tutorRepository.findById(anyLong())).thenReturn(Optional.of(tutor));
    }

    /** Simula que a última dose foi dada há tantas horas. */
    private void ultimaDoseHa(double horas) {
        DoseMedicamento anterior = DoseMedicamento.builder()
                .dataHora(LocalDateTime.now().minusMinutes((long) (horas * 60)))
                .medicamento(remedio)
                .build();

        when(doseRepository.findFirstByMedicamentoIdOrderByDataHoraDesc(1L))
                .thenReturn(Optional.of(anterior));
    }

    @Test
    @DisplayName("primeira dose do tratamento pontua")
    void primeiraDosePontua() {
        when(doseRepository.findFirstByMedicamentoIdOrderByDataHoraDesc(1L))
                .thenReturn(Optional.empty());

        MedicamentoDTO.DoseRegistrada dose = service.registrarDose(1L, null);

        assertThat(dose.pontosGanhos()).isEqualTo(15);
        assertThat(dose.aviso()).isNull();
    }

    @Test
    @DisplayName("dose no horário previsto pontua")
    void doseNoHorarioPontua() {
        ultimaDoseHa(12);

        assertThat(service.registrarDose(1L, null).pontosGanhos()).isEqualTo(15);
    }

    @Test
    @DisplayName("dose dentro dos 20 minutos de tolerância pontua")
    void doseNaToleranciaPontua() {
        ultimaDoseHa(11.75); // faltam 15 minutos para as 12 horas

        assertThat(service.registrarDose(1L, null).pontosGanhos()).isEqualTo(15);
    }

    @Test
    @DisplayName("dose adiantada além da tolerância não pontua")
    void doseAdiantadaNaoPontua() {
        ultimaDoseHa(2);

        MedicamentoDTO.DoseRegistrada dose = service.registrarDose(1L, null);

        assertThat(dose.pontosGanhos()).isZero();
        assertThat(dose.aviso()).contains("adiantada");
        verify(gamificacaoService, never()).registrarAcao(anyLong(), any(), any());
    }

    @Test
    @DisplayName("atraso menor que um intervalo ainda pontua")
    void doseAtrasadaDentroDoIntervaloPontua() {
        ultimaDoseHa(18); // 6 horas de atraso em um intervalo de 12

        assertThat(service.registrarDose(1L, null).pontosGanhos()).isEqualTo(15);
    }

    @Test
    @DisplayName("atraso maior que um intervalo é dose pulada e não pontua")
    void dosePuladaNaoPontua() {
        ultimaDoseHa(25); // 13 horas de atraso em um intervalo de 12

        MedicamentoDTO.DoseRegistrada dose = service.registrarDose(1L, null);

        assertThat(dose.pontosGanhos()).isZero();
        assertThat(dose.aviso()).contains("pulou uma dose");
        verify(gamificacaoService, never()).registrarAcao(anyLong(), any(), any());
    }

    @Test
    @DisplayName("a dose é sempre registrada, pontuando ou não")
    void doseSempreRegistrada() {
        ultimaDoseHa(1);

        service.registrarDose(1L, null);

        verify(doseRepository).save(any(DoseMedicamento.class));
    }
}
