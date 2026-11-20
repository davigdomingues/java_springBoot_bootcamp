package io.github.davigdomingues.paymenthub.pattern.chain;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;
import io.github.davigdomingues.paymenthub.domain.model.RiskAssessment;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * <strong>Padrão GoF: Chain of Responsibility (Elo 2 - Limites de Valor)</strong>
 *
 * <p>Impõe limites operacionais por teto global e por modalidade (Boleto) e sinaliza
 * transações de alto valor adicionando pontuação de risco para monitoramento.</p>
 */
public class AmountLimitValidationHandler extends ComplianceValidationHandler {

    private static final BigDecimal DEFAULT_MAX_GLOBAL_AMOUNT = new BigDecimal("100000.00");
    private static final BigDecimal DEFAULT_MAX_BOLETO_AMOUNT = new BigDecimal("50000.00");
    private static final BigDecimal DEFAULT_HIGH_VALUE_THRESHOLD = new BigDecimal("20000.00");
    private static final int HIGH_VALUE_RISK_POINTS = 35;

    private final BigDecimal maxGlobalAmount;
    private final BigDecimal maxBoletoAmount;
    private final BigDecimal highValueThreshold;

    public AmountLimitValidationHandler() {
        this(DEFAULT_MAX_GLOBAL_AMOUNT, DEFAULT_MAX_BOLETO_AMOUNT, DEFAULT_HIGH_VALUE_THRESHOLD);
    }

    public AmountLimitValidationHandler(
            BigDecimal maxGlobalAmount,
            BigDecimal maxBoletoAmount,
            BigDecimal highValueThreshold
    ) {
        this.maxGlobalAmount = Objects.requireNonNull(maxGlobalAmount, "maxGlobalAmount must not be null");
        this.maxBoletoAmount = Objects.requireNonNull(maxBoletoAmount, "maxBoletoAmount must not be null");
        this.highValueThreshold = Objects.requireNonNull(highValueThreshold, "highValueThreshold must not be null");
    }

    @Override
    protected RiskAssessment evaluate(PaymentContext context, RiskAssessment current) {
        BigDecimal amount = context.amount();
        if (amount.compareTo(maxGlobalAmount) > 0) {
            return current.block("GLOBAL_AMOUNT_LIMIT_EXCEEDED");
        }
        if (context.paymentMethod() == PaymentMethod.BOLETO && amount.compareTo(maxBoletoAmount) > 0) {
            return current.block("BOLETO_AMOUNT_LIMIT_EXCEEDED");
        }
        if (amount.compareTo(highValueThreshold) > 0) {
            return current.withAddedRisk(HIGH_VALUE_RISK_POINTS, "HIGH_VALUE_TRANSACTION");
        }
        return current;
    }
}
