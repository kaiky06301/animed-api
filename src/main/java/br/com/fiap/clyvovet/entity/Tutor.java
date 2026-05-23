package br.com.fiap.clyvovet.entity;

import br.com.fiap.clyvovet.enums.NivelGamificacao;
import br.com.fiap.clyvovet.enums.PlanoAssinatura;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Tutor - Responsável pelo pet.
 * Possui pontuação de gamificação que evolui com ações de cuidado.
 */
@Entity
@Table(name = "TB_TUTOR", uniqueConstraints = {
        @UniqueConstraint(name = "UK_TUTOR_EMAIL", columnNames = "EMAIL"),
        @UniqueConstraint(name = "UK_TUTOR_CPF", columnNames = "CPF")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TUTOR")
    private Long id;

    @Column(name = "NOME", nullable = false, length = 120)
    private String nome;

    @Column(name = "EMAIL", nullable = false, length = 150)
    private String email;

    @Column(name = "CPF", nullable = false, length = 14)
    private String cpf;

    @Column(name = "TELEFONE", length = 20)
    private String telefone;

    @Column(name = "PONTOS_TOTAIS", nullable = false)
    @Builder.Default
    private Integer pontosTotais = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "NIVEL", nullable = false, length = 20)
    @Builder.Default
    private NivelGamificacao nivel = NivelGamificacao.BASICO;

    @Enumerated(EnumType.STRING)
    @Column(name = "PLANO", nullable = false, length = 20)
    @Builder.Default
    private PlanoAssinatura plano = PlanoAssinatura.GRATUITO;

    @Column(name = "DATA_CADASTRO", nullable = false)
    @Builder.Default
    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Column(name = "ATIVO", nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Pet> pets = new ArrayList<>();

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HistoricoPontuacao> historicoPontuacoes = new ArrayList<>();

    /**
     * Adiciona pontos ao tutor e atualiza o nível automaticamente.
     */
    public void adicionarPontos(int pontos) {
        this.pontosTotais += pontos;
        this.nivel = NivelGamificacao.fromPontos(this.pontosTotais);
    }
}
