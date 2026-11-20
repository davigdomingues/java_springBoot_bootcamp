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
 *
 * <p><strong>Estrutura e impacto:</strong> atua como DTO interno imutável entre a camada
 * de execução ({@code PaymentExecutionPipeline}) e a persistência ({@code PaymentTransaction}).
 * Normaliza escalas financeiras para 2 casas decimais ({@code HALF_EVEN}) e mantém cópia
 * imutável de {@code appliedDecorators} para rastreabilidade arquitetural.</p>
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
    /**
     * Valida invariantes obrigatórias e normaliza valores monetários e coleções defensivas.
     *
     * @throws NullPointerException quando {@code transactionCode}, {@code paymentMethod},
     *                              {@code grossAmount}, {@code feeAmount} ou {@code status} forem nulos
     */
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
        // Garante consistência do valor líquido mesmo quando construído diretamente fora do Builder
        netAmount = (netAmount != null ? netAmount : grossAmount.subtract(feeAmount))
                .setScale(2, RoundingMode.HALF_EVEN);
        riskAssessment = riskAssessment != null ? riskAssessment : RiskAssessment.clean();
        appliedDecorators = appliedDecorators == null ? List.of() : List.copyOf(appliedDecorators);
    }
}
