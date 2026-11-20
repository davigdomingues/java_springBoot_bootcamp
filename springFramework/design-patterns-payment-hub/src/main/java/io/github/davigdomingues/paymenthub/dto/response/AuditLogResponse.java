package io.github.davigdomingues.paymenthub.dto.response;

import io.github.davigdomingues.paymenthub.domain.entity.PaymentAuditLog;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.enums.RiskLevel;

import java.time.Instant;

/**
 * DTO de saída para eventos da trilha de auditoria de uma transação.
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
