package br.com.fiap.clyvovet.entity;

import br.com.fiap.clyvovet.enums.TipoAcaoPontuacao;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidade HistoricoPontuacao - Auditoria de cada ação que rendeu pontos.
 * Permite rastrear o engajamento e calcular métricas de retenção.
 */
@Entity
@Table(name = "TB_HISTORICO_PONTUACAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoPontuacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HISTORICO")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_ACAO", nullable = false, length = 40)
    private TipoAcaoPontuacao tipoAcao;

    @Column(name = "PONTOS_GANHOS", nullable = false)
    private Integer pontosGanhos;

    @Column(name = "DESCRICAO", length = 250)
    private String descricao;

    @Column(name = "DATA_HORA", nullable = false)
    @Builder.Default
    private LocalDateTime dataHora = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_TUTOR", nullable = false, foreignKey = @ForeignKey(name = "FK_HIST_TUTOR"))
    private Tutor tutor;
}
