package br.com.fiap.clyvovet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Dose de um medicamento efetivamente dada ao pet pelo tutor. */
@Entity
@Table(name = "TB_DOSE_MEDICAMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoseMedicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DOSE")
    private Long id;

    @Column(name = "DATA_HORA", nullable = false)
    @Builder.Default
    private LocalDateTime dataHora = LocalDateTime.now();

    @Column(name = "OBSERVACAO", length = 250)
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_MEDICAMENTO", nullable = false,
            foreignKey = @ForeignKey(name = "FK_DOSE_REMEDIO"))
    private Medicamento medicamento;
}
