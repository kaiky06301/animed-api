package br.com.fiap.clyvovet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Medicamento prescrito pelo veterinário a um pet.
 *
 * Guarda o que o tutor precisa para cumprir o tratamento: o que dar, de
 * quanto em quanto tempo e até quando. As doses efetivamente administradas
 * ficam em {@link DoseMedicamento}.
 */
@Entity
@Table(name = "TB_MEDICAMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MEDICAMENTO")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 120)
    private String nome;

    /** Quanto dar por vez, como o veterinário escreveu: "1 comprimido", "5 ml". */
    @Column(name = "DOSAGEM", length = 60)
    private String dosagem;

    @Column(name = "INTERVALO_HORAS", nullable = false)
    private Integer intervaloHoras;

    @Column(name = "DATA_INICIO", nullable = false)
    private LocalDate dataInicio;

    /** Último dia do tratamento; nulo quando é de uso contínuo. */
    @Column(name = "DATA_FIM")
    private LocalDate dataFim;

    @Column(name = "OBSERVACAO", length = 250)
    private String observacao;

    @Column(name = "DATA_CADASTRO", nullable = false)
    @Builder.Default
    private LocalDateTime dataCadastro = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PET", nullable = false,
            foreignKey = @ForeignKey(name = "FK_MEDICAMENTO_PET"))
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_VETERINARIO",
            foreignKey = @ForeignKey(name = "FK_MEDICAMENTO_VET"))
    private Usuario veterinario;

    @OneToMany(mappedBy = "medicamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DoseMedicamento> doses = new ArrayList<>();

    /** O tratamento vale para a data informada? */
    public boolean estaEmCurso(LocalDate data) {
        boolean comecou = !data.isBefore(dataInicio);
        boolean naoTerminou = dataFim == null || !data.isAfter(dataFim);
        return comecou && naoTerminou;
    }
}
