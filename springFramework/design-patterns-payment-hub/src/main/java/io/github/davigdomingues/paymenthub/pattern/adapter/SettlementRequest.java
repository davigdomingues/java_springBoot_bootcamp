package io.github.davigdomingues.paymenthub.pattern.adapter;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Requisição moderna e tipada enviada à porta de liquidação externa.
 */
public record SettlementRequest(
        String transactionCode,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String payerDocument,
        String payeeDocument
) {
    public SettlementRequest {
        Objects.requireNonNull(transactionCode, "transactionCode must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(paymentMethod, "paymentMethod must not be null");
        Objects.requireNonNull(payerDocument, "payerDocument must not be null");
        Objects.requireNonNull(payeeDocument, "payeeDocument must not be null");
    }
}
