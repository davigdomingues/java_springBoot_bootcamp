package io.github.davigdomingues.paymenthub.pattern.strategy;

import io.github.davigdomingues.paymenthub.domain.enums.CustomerTier;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;
import io.github.davigdomingues.paymenthub.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <strong>Padrão GoF: Strategy (Implementação Concreta - PIX)</strong>
 *
 * <p>Processa transferências instantâneas via chave PIX, com isenção tarifária
 * para clientes VIP ou transações até R$ 5.000,00 e teto de R$ 25,00 acima desse limite.</p>
 */
@Component
public class PixPaymentStrategy implements PaymentStrategy {

    private static final BigDecimal EXEMPTION_THRESHOLD = new BigDecimal("5000.00");
    private static final BigDecimal FEE_RATE = new BigDecimal("0.0025");
    private static final BigDecimal FEE_CAP = new BigDecimal("25.00");

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.PIX;
    }

    @Override
    public void validateSpecificRules(PaymentContext context) {
        if (context.pixKey() == null || context.pixKey().isBlank()) {
            throw new BusinessRuleException("PIX payments require a non-blank pixKey");
        }
        if (context.installments() != 1) {
            throw new BusinessRuleException("PIX payments do not support installments greater than 1");
        }
    }

    @Override
    public BigDecimal calculateFee(PaymentContext context) {
        if (context.customerTier() == CustomerTier.VIP
                || context.amount().compareTo(EXEMPTION_THRESHOLD) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        }
        BigDecimal computed = context.amount()
                .multiply(FEE_RATE)
                .setScale(2, RoundingMode.HALF_EVEN);
        return computed.min(FEE_CAP);
    }

    @Override
    public PaymentStatus resolveInitialStatus() {
        return PaymentStatus.APPROVED;
    }

    @Override
    public String settlementWindowDescription() {
        return "Instant (24/7 via SPI/Pix)";
    }
}
