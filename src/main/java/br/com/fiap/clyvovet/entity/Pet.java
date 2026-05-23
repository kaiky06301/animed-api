package br.com.fiap.clyvovet.entity;

import br.com.fiap.clyvovet.enums.Especie;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Pet - Animal de estimação cadastrado na plataforma.
 * Núcleo do histórico longitudinal de saúde.
 */
@Entity
@Table(name = "TB_PET")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PET")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 80)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESPECIE", nullable = false, length = 20)
    private Especie especie;

    @Column(name = "RACA", length = 80)
    private String raca;

    @Column(name = "DATA_NASCIMENTO")
    private LocalDate dataNascimento;

    @Column(name = "PESO_KG", precision = 5, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "CASTRADO")
    @Builder.Default
    private Boolean castrado = false;

    @Column(name = "OBSERVACOES_SAUDE", length = 1000)
    private String observacoesSaude;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_TUTOR", nullable = false, foreignKey = @ForeignKey(name = "FK_PET_TUTOR"))
    private Tutor tutor;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Vacina> vacinas = new ArrayList<>();

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Consulta> consultas = new ArrayList<>();
}
