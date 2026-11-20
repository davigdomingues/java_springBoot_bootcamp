package io.github.davigdomingues.paymenthub.domain.enums;

/**
 * Estados possíveis do ciclo de vida de uma transação de pagamento.
 */
public enum PaymentStatus {
    APPROVED,
    PENDING_SETTLEMENT,
    REJECTED,
    REFUNDED
}
