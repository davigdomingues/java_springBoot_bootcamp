package io.github.davigdomingues.paymenthub.dto.response;

import io.github.davigdomingues.paymenthub.domain.entity.PaymentAuditLog;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.enums.RiskLevel;

import java.time.Instant;

/**
 * DTO imutável de saída para eventos da trilha cronológica de auditoria de uma transação.
 *
 * Estrutura e impacto: projeta a entidade {@link PaymentAuditLog} para a borda REST desacoplando o
 * modelo relacional JPA da serialização JSON do endpoint
 * {@code GET /api/v1/payments/{transactionCode}/audit}.
 */
public record AuditLogResponse(
        Long id,
        String transactionCode,
        String eventType,
        PaymentStatus status,
        RiskLevel riskLevel,
        String details,
        Instant recordedAt
) {
    public static AuditLogResponse fromEntity(PaymentAuditLog entity) {
        return new AuditLogResponse(
                entity.getId(),
                entity.getTransactionCode(),
                entity.getEventType(),
                entity.getStatus(),
                entity.getRiskLevel(),
                entity.getDetails(),
                entity.getRecordedAt()
        );
    }
}
