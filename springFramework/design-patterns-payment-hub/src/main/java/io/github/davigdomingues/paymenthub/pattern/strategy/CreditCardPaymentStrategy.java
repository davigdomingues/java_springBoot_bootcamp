package io.github.davigdomingues.paymenthub.pattern.strategy;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;
import io.github.davigdomingues.paymenthub.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <strong>Padrão GoF: Strategy (Implementação Concreta - Cartão de Crédito)</strong>
 *
 * <p>Suporta parcelamento de 1 a 12 vezes com alíquota progressiva por parcela,
 * descontos por nível de relacionamento (PREMIUM/VIP) e piso mínimo de 0,99%.</p>
 */
@Component
public class CreditCardPaymentStrategy implements PaymentStrategy {

    private static final int MIN_INSTALLMENTS = 1;
    private static final int MAX_INSTALLMENTS = 12;
    private static final BigDecimal BASE_RATE = new BigDecimal("0.0249");
    private static final BigDecimal INSTALLMENT_INCREMENT_RATE = new BigDecimal("0.0050");
    private static final BigDecimal PREMIUM_DISCOUNT_RATE = new BigDecimal("0.0050");
    private static final BigDecimal VIP_DISCOUNT_RATE = new BigDecimal("0.0100");
    private static final BigDecimal FLOOR_RATE = new BigDecimal("0.0099");

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public void validateSpecificRules(PaymentContext context) {
        if (context.installments() < MIN_INSTALLMENTS || context.installments() > MAX_INSTALLMENTS) {
            throw new BusinessRuleException("Credit card installments must be between 1 and 12");
        }
    }

    @Override
    public BigDecimal calculateFee(PaymentContext context) {
        BigDecimal installmentIncrement = INSTALLMENT_INCREMENT_RATE
                .multiply(BigDecimal.valueOf(context.installments() - 1L));
        BigDecimal tierDiscount = switch (context.customerTier()) {
            case STANDARD -> BigDecimal.ZERO;
            case PREMIUM -> PREMIUM_DISCOUNT_RATE;
            case VIP -> VIP_DISCOUNT_RATE;
        };

        BigDecimal effectiveRate = BASE_RATE
                .add(installmentIncrement)
                .subtract(tierDiscount)
                .max(FLOOR_RATE);

        return context.amount()
                .multiply(effectiveRate)
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    public PaymentStatus resolveInitialStatus() {
        return PaymentStatus.APPROVED;
    }

    @Override
    public String settlementWindowDescription() {
        return "D+1 business day (card acquirer)";
    }
}
