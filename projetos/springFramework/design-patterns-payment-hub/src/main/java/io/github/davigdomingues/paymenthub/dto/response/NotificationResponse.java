package io.github.davigdomingues.paymenthub.dto.response;

import io.github.davigdomingues.paymenthub.domain.entity.NotificationLog;

import java.time.Instant;

/**
 * DTO de saída para registros de notificações emitidas pelos observadores.
 */
public record NotificationResponse(
        Long id,
        String transactionCode,
        String channel,
        String recipient,
        String subject,
        String message,
        Instant dispatchedAt
) {
    public static NotificationResponse fromEntity(NotificationLog entity) {
        return new NotificationResponse(
                entity.getId(),
                entity.getTransactionCode(),
                entity.getChannel(),
                entity.getRecipient(),
                entity.getSubject(),
                entity.getMessage(),
                entity.getDispatchedAt()
        );
    }
}
