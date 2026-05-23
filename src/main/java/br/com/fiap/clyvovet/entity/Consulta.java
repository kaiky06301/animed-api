package br.com.fiap.clyvovet.entity;

import br.com.fiap.clyvovet.enums.StatusConsulta;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade Consulta - Agendamento/registro de consulta veterinária.
 * Eventos de consulta alimentam o histórico longitudinal e geram pontos.
 */
@Entity
@Table(name = "TB_CONSULTA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CONSULTA")
    private Long id;

    @Column(name = "DATA_HORA", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "MOTIVO", nullable = false, length = 250)
    private String motivo;

    @Column(name = "DIAGNOSTICO", length = 1000)
    private String diagnostico;

    @Column(name = "PRESCRICAO", length = 1000)
    private String prescricao;

    @Column(name = "VALOR", precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    @Builder.Default
    private StatusConsulta status = StatusConsulta.AGENDADA;

    @Column(name = "VETERINARIO", length = 120)
    private String veterinario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PET", nullable = false, foreignKey = @ForeignKey(name = "FK_CONSULTA_PET"))
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PETSHOP", foreignKey = @ForeignKey(name = "FK_CONSULTA_PETSHOP"))
    private PetShop petShop;
}
