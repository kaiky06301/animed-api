package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.TransacaoParceiroDTO;
import br.com.fiap.clyvovet.entity.PetShop;
import br.com.fiap.clyvovet.entity.TransacaoParceiro;
import br.com.fiap.clyvovet.entity.Tutor;
import br.com.fiap.clyvovet.enums.TipoAcaoPontuacao;
import br.com.fiap.clyvovet.exception.BusinessException;
import br.com.fiap.clyvovet.exception.ResourceNotFoundException;
import br.com.fiap.clyvovet.repository.TransacaoParceiroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service de Transações com Parceiros.
 *
 * REGRA DE NEGÓCIO CHAVE (diferencial Clyvo VET):
 * - Tutor com pontos ganha desconto baseado no NÍVEL
 * - Cliente sem pontos paga 100% do valor
 * - Cliente NIVEL CUIDADOR ganha 5% de desconto
 * - Cliente TUTOR_PREMIUM ganha 15% de desconto
 * - Sobre o valor final (já com desconto), Clyvo cobra comissão do pet shop
 */
@Service
@RequiredArgsConstructor
public class TransacaoParceiroService {

    private final TransacaoParceiroRepository transacaoRepository;
    private final TutorService tutorService;
    private final PetShopService petShopService;
    private final GamificacaoService gamificacaoService;

    /**
     * Quanto vale cada moeda em reais.
     *
     * Dez moedas por real mantém o benefício visível sem transformar a
     * pontuação em dinheiro fácil.
     */
    private static final BigDecimal VALOR_DA_MOEDA = new BigDecimal("0.10");

    /**
     * Teto do abatimento por compra.
     *
     * Sem limite, um saldo acumulado zeraria o valor e o parceiro receberia
     * nada pela venda — o desconto é um incentivo, não um vale-compras.
     */
    private static final BigDecimal TETO_ABATIMENTO = new BigDecimal("0.50");

    @Transactional
    public TransacaoParceiroDTO.Response criar(TransacaoParceiroDTO.Request request) {
        Tutor tutor = tutorService.buscarEntidade(request.idTutor());
        PetShop petShop = petShopService.buscarEntidade(request.idPetShop());

        // === Cálculo do desconto baseado no nível do tutor ===
        BigDecimal valorBruto = request.valorBruto();
        BigDecimal descontoPercentual = BigDecimal.valueOf(tutor.getNivel().getDescontoPercentual());
        BigDecimal desconto = valorBruto
                .multiply(descontoPercentual)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal subtotal = valorBruto.subtract(desconto);

        // === Abatimento pelas moedas, quando o tutor optar por usá-las ===
        int moedasUsadas = request.moedasUsadas() == null ? 0 : request.moedasUsadas();
        BigDecimal abatimento = BigDecimal.ZERO;

        if (moedasUsadas > 0) {
            if (!tutor.podeGastarMoedas()) {
                throw new BusinessException(
                        "As moedas são liberadas no nível Tutor Premium");
            }

            if (moedasUsadas > tutor.getMoedas()) {
                throw new BusinessException("Você tem apenas "
                        + tutor.getMoedas() + " moedas disponíveis");
            }

            BigDecimal teto = subtotal.multiply(TETO_ABATIMENTO).setScale(2, RoundingMode.HALF_UP);
            abatimento = BigDecimal.valueOf(moedasUsadas)
                    .multiply(VALOR_DA_MOEDA)
                    .setScale(2, RoundingMode.HALF_UP);

            if (abatimento.compareTo(teto) > 0) {
                throw new BusinessException(
                        "As moedas podem abater no máximo metade do valor da compra");
            }

            tutor.gastarMoedas(moedasUsadas);
        }

        BigDecimal valorFinal = subtotal.subtract(abatimento);

        // Comissão Clyvo sobre o valor final
        BigDecimal comissao = valorFinal
                .multiply(petShop.getComissaoPercentual())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Pontos: 1 ponto a cada R$ 10 gastos
        int pontosGerados = valorFinal.intValue() / 10;
        if (pontosGerados < 1) pontosGerados = 1;

        TransacaoParceiro transacao = TransacaoParceiro.builder()
                .tutor(tutor)
                .petShop(petShop)
                .valorBruto(valorBruto)
                .descontoAplicado(desconto)
                .moedasUsadas(moedasUsadas)
                .abatimentoMoedas(abatimento)
                .valorFinal(valorFinal)
                .comissaoClyvo(comissao)
                .pontosGerados(pontosGerados)
                .descricaoProduto(request.descricaoProduto())
                .build();

        TransacaoParceiro salva = transacaoRepository.save(transacao);

        // Gamifica a compra
        gamificacaoService.registrarAcaoCustomizada(
                tutor.getId(),
                TipoAcaoPontuacao.COMPRA_PARCEIRO,
                pontosGerados,
                "Compra em " + petShop.getNomeFantasia() + ": " + request.descricaoProduto()
        );

        return toResponse(salva);
    }

    public TransacaoParceiroDTO.Response buscarPorId(Long id) {
        return toResponse(buscarEntidade(id));
    }

    public TransacaoParceiro buscarEntidade(Long id) {
        return transacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação", id));
    }

    public Page<TransacaoParceiroDTO.Response> listar(Pageable pageable) {
        return transacaoRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<TransacaoParceiroDTO.Response> listarPorTutor(Long idTutor, Pageable pageable) {
        return transacaoRepository.findByTutorId(idTutor, pageable).map(this::toResponse);
    }

    public Page<TransacaoParceiroDTO.Response> listarPorPetShop(Long idPetShop, Pageable pageable) {
        return transacaoRepository.findByPetShopId(idPetShop, pageable).map(this::toResponse);
    }

    public BigDecimal totalComissaoPorPetShop(Long idPetShop) {
        return transacaoRepository.totalComissaoPorPetShop(idPetShop);
    }

    public BigDecimal totalGastoPorTutor(Long idTutor) {
        return transacaoRepository.totalGastoPorTutor(idTutor);
    }

    @Transactional
    public void deletar(Long id) {
        if (!transacaoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transação", id);
        }
        transacaoRepository.deleteById(id);
    }

    private TransacaoParceiroDTO.Response toResponse(TransacaoParceiro t) {
        return new TransacaoParceiroDTO.Response(
                t.getId(),
                t.getDataHora(),
                t.getValorBruto(),
                t.getDescontoAplicado(),
                t.getValorFinal(),
                t.getComissaoClyvo(),
                t.getMoedasUsadas(),
                t.getAbatimentoMoedas(),
                t.getPontosGerados(),
                t.getDescricaoProduto(),
                t.getTutor().getId(),
                t.getTutor().getNome(),
                t.getPetShop().getId(),
                t.getPetShop().getNomeFantasia()
        );
    }
}
