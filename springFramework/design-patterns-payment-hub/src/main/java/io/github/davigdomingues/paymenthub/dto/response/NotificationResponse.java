package io.github.davigdomingues.paymenthub.dto.response;

import io.github.davigdomingues.paymenthub.domain.entity.NotificationLog;

import java.time.Instant;

/**
 * DTO imutável de saída para registros de notificações emitidas pelos observadores.
 *
 * <p><strong>Estrutura e impacto:</strong> isola a entidade JPA {@link NotificationLog} na
 * consulta de {@code GET /api/v1/payments/{transactionCode}/notifications}, expondo canais
 * ({@code PUSH}, {@code EMAIL}, {@code FRAUD_ALERT_WEBHOOK}) sem vazar metadados de persistência.</p>
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
