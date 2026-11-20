package io.github.davigdomingues.paymenthub.domain.model;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.enums.RiskLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Evento de domínio imutável emitido pelo Publisher do padrão Observer
 * após processamento, rejeição ou estorno de uma transação.
 */
public record PaymentProcessedEvent(
        String transactionCode,
        String eventType,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        BigDecimal grossAmount,
        BigDecimal feeAmount,
        BigDecimal cashbackAmount,
        String payerDocument,
        String payeeDocument,
        RiskLevel riskLevel,
        Instant occurredAt,
        String details
) {
    public PaymentProcessedEvent {
        Objects.requireNonNull(transactionCode, "transactionCode must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(paymentMethod, "paymentMethod must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(riskLevel, "riskLevel must not be null");
        occurredAt = occurredAt != null ? occurredAt : Instant.now();
    }
}
