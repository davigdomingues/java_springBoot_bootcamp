package io.github.davigdomingues.paymenthub.pattern.decorator;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;
import io.github.davigdomingues.paymenthub.domain.model.PaymentExecutionResult;
import io.github.davigdomingues.paymenthub.domain.model.RiskAssessment;
import io.github.davigdomingues.paymenthub.pattern.builder.PaymentReceiptBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * <strong>Padrão GoF: Decorator (Concrete Decorator - Benefício de Cashback)</strong>
 *
 * <p>Calcula e injeta o benefício de cashback sobre o resultado produzido pelo núcleo,
 * preservando a imutabilidade via {@link PaymentReceiptBuilder#from(PaymentExecutionResult)}.</p>
 */
public class CashbackBenefitDecorator extends AbstractPaymentPipelineDecorator {

    private static final String DECORATOR_NAME = "CashbackBenefitDecorator";
    private static final BigDecimal VIP_RATE = new BigDecimal("0.02");
    private static final BigDecimal VIP_CAP = new BigDecimal("100.00");
    private static final BigDecimal PREMIUM_RATE = new BigDecimal("0.01");
    private static final BigDecimal PREMIUM_CAP = new BigDecimal("50.00");
    private static final BigDecimal PREMIUM_MIN_AMOUNT = new BigDecimal("200.00");

    public CashbackBenefitDecorator(PaymentExecutionPipeline delegate) {
        super(delegate);
    }

    @Override
    public PaymentExecutionResult execute(PaymentContext context, RiskAssessment riskAssessment) {
        PaymentExecutionResult baseResult = delegate.execute(context, riskAssessment);
        BigDecimal cashback = calculateCashback(context);

        return PaymentReceiptBuilder.from(baseResult)
                .cashbackAmount(cashback)
                .addDecoratorApplied(DECORATOR_NAME)
                .build();
    }

    /**
     * Calcula o valor de cashback elegível de acordo com o nível do cliente,
     * meio de pagamento e valor da operação.
     *
     * @param context contexto imutável do pagamento
     * @return valor de cashback arredondado com 2 casas decimais
     */
    public static BigDecimal calculateCashback(PaymentContext context) {
        Objects.requireNonNull(context, "context must not be null");
        return switch (context.customerTier()) {
            case VIP -> context.amount()
                    .multiply(VIP_RATE)
                    .setScale(2, RoundingMode.HALF_EVEN)
                    .min(VIP_CAP);
            case PREMIUM -> isEligibleForPremiumCashback(context)
                    ? context.amount()
                            .multiply(PREMIUM_RATE)
                            .setScale(2, RoundingMode.HALF_EVEN)
                            .min(PREMIUM_CAP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
            case STANDARD -> BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        };
    }

    private static boolean isEligibleForPremiumCashback(PaymentContext context) {
        return context.paymentMethod() == PaymentMethod.PIX
                || context.amount().compareTo(PREMIUM_MIN_AMOUNT) >= 0;
    }
}
