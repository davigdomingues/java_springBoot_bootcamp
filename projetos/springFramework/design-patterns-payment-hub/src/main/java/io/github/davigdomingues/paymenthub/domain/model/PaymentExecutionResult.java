package io.github.davigdomingues.paymenthub.domain.model;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Resultado imutável consolidado pelo pipeline de execução (Core + Decorators)
 * e construído por meio do {@code PaymentReceiptBuilder}.
 */
public record PaymentExecutionResult(
        String transactionCode,
        PaymentMethod paymentMethod,
        BigDecimal grossAmount,
        BigDecimal feeAmount,
        BigDecimal cashbackAmount,
        BigDecimal netAmount,
        PaymentStatus status,
        String settlementWindow,
        String externalAuthorizationCode,
        String clearingChannel,
        RiskAssessment riskAssessment,
        long processingTimeMs,
        List<String> appliedDecorators
) {
    public PaymentExecutionResult {
        Objects.requireNonNull(transactionCode, "transactionCode must not be null");
        Objects.requireNonNull(paymentMethod, "paymentMethod must not be null");
        Objects.requireNonNull(grossAmount, "grossAmount must not be null");
        Objects.requireNonNull(feeAmount, "feeAmount must not be null");
        Objects.requireNonNull(status, "status must not be null");
        grossAmount = grossAmount.setScale(2, RoundingMode.HALF_EVEN);
        feeAmount = feeAmount.setScale(2, RoundingMode.HALF_EVEN);
        cashbackAmount = (cashbackAmount != null ? cashbackAmount : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_EVEN);
        netAmount = (netAmount != null ? netAmount : grossAmount.subtract(feeAmount))
                .setScale(2, RoundingMode.HALF_EVEN);
        riskAssessment = riskAssessment != null ? riskAssessment : RiskAssessment.clean();
        appliedDecorators = appliedDecorators == null ? List.of() : List.copyOf(appliedDecorators);
    }
}
