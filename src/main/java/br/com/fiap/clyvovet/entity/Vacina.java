package br.com.fiap.clyvovet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entidade Vacina - Registro de vacinação aplicada no pet.
 * Cada registro gera pontos para o tutor (gamificação preventiva).
 */
@Entity
@Table(name = "TB_VACINA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vacina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_VACINA")
    private Long id;

    @Column(name = "NOME_VACINA", nullable = false, length = 100)
    private String nomeVacina;

    @Column(name = "DATA_APLICACAO", nullable = false)
    private LocalDate dataAplicacao;

    @Column(name = "DATA_PROXIMA_DOSE")
    private LocalDate dataProximaDose;

    @Column(name = "VETERINARIO_RESPONSAVEL", length = 120)
    private String veterinarioResponsavel;

    @Column(name = "CLINICA", length = 150)
    private String clinica;

    @Column(name = "LOTE", length = 50)
    private String lote;

    @Column(name = "OBSERVACOES", length = 500)
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PET", nullable = false, foreignKey = @ForeignKey(name = "FK_VACINA_PET"))
    private Pet pet;
}
