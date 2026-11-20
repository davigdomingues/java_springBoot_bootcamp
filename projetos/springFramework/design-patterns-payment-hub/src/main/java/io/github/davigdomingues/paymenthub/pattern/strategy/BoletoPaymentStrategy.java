package io.github.davigdomingues.paymenthub.pattern.strategy;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;
import io.github.davigdomingues.paymenthub.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <strong>Padrão GoF: Strategy (Implementação Concreta - Boleto Bancário)</strong>
 *
 * <p>Aplica tarifa fixa por nível do cliente, exige valor mínimo de R$ 10,00
 * à vista e inicia a transação aguardando compensação bancária.</p>
 */
@Component
public class BoletoPaymentStrategy implements PaymentStrategy {

    private static final BigDecimal MINIMUM_BOLETO_AMOUNT = new BigDecimal("10.00");
    private static final BigDecimal STANDARD_FEE = new BigDecimal("3.50");
    private static final BigDecimal PREMIUM_FEE = new BigDecimal("1.75");
    private static final BigDecimal VIP_FEE = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.BOLETO;
    }

    @Override
    public void validateSpecificRules(PaymentContext context) {
        if (context.installments() != 1) {
            throw new BusinessRuleException("Boleto payments do not support installments greater than 1");
        }
        if (context.amount().compareTo(MINIMUM_BOLETO_AMOUNT) < 0) {
            throw new BusinessRuleException("Boleto payments require a minimum amount of 10.00");
        }
    }

    @Override
    public BigDecimal calculateFee(PaymentContext context) {
        return switch (context.customerTier()) {
            case STANDARD -> STANDARD_FEE;
            case PREMIUM -> PREMIUM_FEE;
            case VIP -> VIP_FEE;
        };
    }

    @Override
    public PaymentStatus resolveInitialStatus() {
        return PaymentStatus.PENDING_SETTLEMENT;
    }

    @Override
    public String settlementWindowDescription() {
        return "Up to 2 business days (CIP clearing)";
    }
}
