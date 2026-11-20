package io.github.davigdomingues.paymenthub.pattern.decorator;

import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;
import io.github.davigdomingues.paymenthub.domain.model.PaymentExecutionResult;
import io.github.davigdomingues.paymenthub.domain.model.RiskAssessment;
import io.github.davigdomingues.paymenthub.pattern.adapter.ExternalSettlementPort;
import io.github.davigdomingues.paymenthub.pattern.adapter.SettlementRequest;
import io.github.davigdomingues.paymenthub.pattern.adapter.SettlementResponse;
import io.github.davigdomingues.paymenthub.pattern.builder.PaymentReceiptBuilder;
import io.github.davigdomingues.paymenthub.pattern.factory.PaymentStrategyFactory;
import io.github.davigdomingues.paymenthub.pattern.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * <strong>Padrão GoF: Decorator (Concrete Component)</strong>
 *
 * <p>
 * <strong>Estrutura e ponto de maior impacto:</strong> representa o núcleo
 * indivisível
 * de execução após aprovação na Chain of Responsibility. Integra quatro padrões
 * em sequência:
 * resolve a {@code PaymentStrategy} via Factory, calcula tarifa e valor líquido
 * ({@code HALF_EVEN}),
 * efetiva a liquidação no mainframe via {@link ExternalSettlementPort}
 * (Adapter) e constrói o
 * recibo base imutável via {@link PaymentReceiptBuilder}. Os decorators
 * externos enriquecem
 * esse recibo sem modificar esta classe.
 * </p>
 */
@Component
public class CorePaymentExecutionPipeline implements PaymentExecutionPipeline {

    private final PaymentStrategyFactory strategyFactory;
    private final ExternalSettlementPort settlementPort;

    public CorePaymentExecutionPipeline(
            PaymentStrategyFactory strategyFactory,
            ExternalSettlementPort settlementPort) {
        this.strategyFactory = Objects.requireNonNull(strategyFactory, "strategyFactory must not be null");
        this.settlementPort = Objects.requireNonNull(settlementPort, "settlementPort must not be null");
    }

    /**
     * Executa tarifa, liquidação externa e montagem do recibo base com cashback
     * inicial zerado.
     *
     * @throws NullPointerException                                                            quando
     *                                                                                         {@code context}
     *                                                                                         ou
     *                                                                                         {@code riskAssessment}
     *                                                                                         forem
     *                                                                                         nulos
     * @throws io.github.davigdomingues.paymenthub.exception.UnsupportedPaymentMethodException
     *                                                                                         se
     *                                                                                         o
     *                                                                                         método
     *                                                                                         não
     *                                                                                         possuir
     *                                                                                         estratégia
     *                                                                                         registrada
     * @throws io.github.davigdomingues.paymenthub.exception.BusinessRuleException
     *                                                                                         se
     *                                                                                         a
     *                                                                                         liquidação
     *                                                                                         no
     *                                                                                         adaptador
     *                                                                                         legado
     *                                                                                         falhar
     */
    @Override
    public PaymentExecutionResult execute(PaymentContext context, RiskAssessment riskAssessment) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(riskAssessment, "riskAssessment must not be null");

        PaymentStrategy strategy = strategyFactory.getStrategy(context.paymentMethod());
        BigDecimal feeAmount = strategy.calculateFee(context).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal netAmount = context.amount().subtract(feeAmount).setScale(2, RoundingMode.HALF_EVEN);

        SettlementResponse settlement = settlementPort.settle(new SettlementRequest(
                context.transactionCode(),
                context.amount(),
                context.paymentMethod(),
                context.payerDocument(),
                context.payeeDocument()));

        return PaymentReceiptBuilder.builder()
                .transactionCode(context.transactionCode())
                .paymentMethod(context.paymentMethod())
                .grossAmount(context.amount())
                .feeAmount(feeAmount)
                .cashbackAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN))
                .netAmount(netAmount)
                .status(strategy.resolveInitialStatus())
                .settlementWindow(strategy.settlementWindowDescription())
                .externalAuthorizationCode(settlement.authorizationCode())
                .clearingChannel(settlement.clearingChannel())
                .riskAssessment(riskAssessment)
                .build();
    }
}
