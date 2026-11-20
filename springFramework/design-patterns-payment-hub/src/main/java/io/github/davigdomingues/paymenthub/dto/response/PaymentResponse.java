package io.github.davigdomingues.paymenthub.dto.response;

import io.github.davigdomingues.paymenthub.domain.entity.PaymentTransaction;
import io.github.davigdomingues.paymenthub.domain.enums.CustomerTier;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.enums.RiskLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Representação imutável de saída para processamento, consulta e estorno de transações.
 *
 * Estrutura e impacto: consolida valores financeiros, avaliação de risco, canal legado, janela de
 * liquidação e decorators aplicados em um contrato JSON estável, evitando serialização direta da
 * entidade JPA {@link PaymentTransaction} (especialmente com
 * {@code spring.jpa.open-in-view=false}).
 */
public record PaymentResponse(
        String transactionCode,
        String idempotencyKey,
        String payerDocument,
        String payeeDocument,
        PaymentMethod paymentMethod,
        CustomerTier customerTier,
        int installments,
        BigDecimal grossAmount,
        BigDecimal feeAmount,
        BigDecimal cashbackAmount,
        BigDecimal netAmount,
        PaymentStatus status,
        RiskLevel riskLevel,
        int riskScore,
        List<String> riskFlags,
        String externalAuthorizationCode,
        String clearingChannel,
        String settlementWindow,
        List<String> appliedDecorators,
        long processingTimeMs,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Converte a entidade JPA {@link PaymentTransaction} para DTO de resposta.
     *
     * @param entity entidade persistida
     * @return DTO imutável correspondente
     */
    public static PaymentResponse fromEntity(PaymentTransaction entity) {
        return new PaymentResponse(
                entity.getTransactionCode(),
                entity.getIdempotencyKey(),
                entity.getPayerDocument(),
                entity.getPayeeDocument(),
                entity.getPaymentMethod(),
                entity.getCustomerTier(),
                entity.getInstallments(),
                entity.getGrossAmount(),
                entity.getFeeAmount(),
                entity.getCashbackAmount(),
                entity.getNetAmount(),
                entity.getStatus(),
                entity.getRiskLevel(),
                entity.getRiskScore(),
                entity.getRiskFlagsList(),
                entity.getExternalAuthorizationCode(),
                entity.getClearingChannel(),
                entity.getSettlementWindow(),
                entity.getAppliedDecoratorsList(),
                entity.getProcessingTimeMs(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
