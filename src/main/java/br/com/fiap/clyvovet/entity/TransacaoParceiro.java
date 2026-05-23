package br.com.fiap.clyvovet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Entidade TransacaoParceiro - Compra realizada por um tutor em pet shop parceiro.
 * Gera pontos para o tutor e comissão para a Clyvo VET.
 */
@Entity
@Table(name = "TB_TRANSACAO_PARCEIRO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransacaoParceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TRANSACAO")
    private Long id;

    @Column(name = "DATA_HORA", nullable = false)
    @Builder.Default
    private LocalDateTime dataHora = LocalDateTime.now();

    @Column(name = "VALOR_BRUTO", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorBruto;

    @Column(name = "DESCONTO_APLICADO", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal descontoAplicado = BigDecimal.ZERO;

    @Column(name = "VALOR_FINAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFinal;

    @Column(name = "COMISSAO_CLYVO", nullable = false, precision = 10, scale = 2)
    private BigDecimal comissaoClyvo;

    @Column(name = "PONTOS_GERADOS", nullable = false)
    private Integer pontosGerados;

    @Column(name = "DESCRICAO_PRODUTO", length = 250)
    private String descricaoProduto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_TUTOR", nullable = false, foreignKey = @ForeignKey(name = "FK_TRANSACAO_TUTOR"))
    private Tutor tutor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_PETSHOP", nullable = false, foreignKey = @ForeignKey(name = "FK_TRANSACAO_PETSHOP"))
    private PetShop petShop;

    /**
     * Calcula valor final e comissão automaticamente antes de persistir.
     */
    @PrePersist
    public void calcularValores() {
        if (this.descontoAplicado == null) this.descontoAplicado = BigDecimal.ZERO;
        this.valorFinal = this.valorBruto.subtract(this.descontoAplicado);
        if (this.petShop != null) {
            this.comissaoClyvo = this.valorFinal
                    .multiply(this.petShop.getComissaoPercentual())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        }
    }
}
