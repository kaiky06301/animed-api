package br.com.fiap.clyvovet.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade PetShop - Parceiro B2B da plataforma Clyvo VET.
 * Modelo híbrido: taxa fixa baixa + comissão por performance sobre vendas.
 */
@Entity
@Table(name = "TB_PETSHOP", uniqueConstraints = {
        @UniqueConstraint(name = "UK_PETSHOP_CNPJ", columnNames = "CNPJ")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PETSHOP")
    private Long id;

    @Column(name = "RAZAO_SOCIAL", nullable = false, length = 150)
    private String razaoSocial;

    @Column(name = "NOME_FANTASIA", nullable = false, length = 120)
    private String nomeFantasia;

    @Column(name = "CNPJ", nullable = false, length = 18)
    private String cnpj;

    @Column(name = "ENDERECO", length = 250)
    private String endereco;

    @Column(name = "CIDADE", length = 100)
    private String cidade;

    @Column(name = "UF", length = 2)
    private String uf;

    @Column(name = "TELEFONE", length = 20)
    private String telefone;

    @Column(name = "TAXA_MENSAL", precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal taxaMensal = new BigDecimal("99.90");

    @Column(name = "COMISSAO_PERCENTUAL", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal comissaoPercentual = new BigDecimal("5.00");

    @Column(name = "ATIVO", nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "DATA_PARCERIA", nullable = false)
    @Builder.Default
    private LocalDateTime dataParceria = LocalDateTime.now();

    @OneToMany(mappedBy = "petShop", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransacaoParceiro> transacoes = new ArrayList<>();
}
