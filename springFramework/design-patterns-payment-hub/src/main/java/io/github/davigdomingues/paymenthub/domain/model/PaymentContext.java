package io.github.davigdomingues.paymenthub.domain.model;

import io.github.davigdomingues.paymenthub.domain.enums.CustomerTier;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Contexto imutável de entrada compartilhado entre os padrões Strategy,
 * Chain of Responsibility, Decorator e Adapter durante o processamento de um pagamento.
 */
public record PaymentContext(
        String transactionCode,
        String idempotencyKey,
        String payerDocument,
        String payeeDocument,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        CustomerTier customerTier,
        int installments,
        String pixKey,
        String description
) {
    /**
     * Construtor compacto responsável por validar campos obrigatórios e normalizar
     * valores financeiros e defaults de domínio.
     */
    public PaymentContext {
        transactionCode = requireNonBlank(transactionCode, "transactionCode");
        payerDocument = requireNonBlank(payerDocument, "payerDocument");
        payeeDocument = requireNonBlank(payeeDocument, "payeeDocument");
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
        Objects.requireNonNull(paymentMethod, "paymentMethod must not be null");
        customerTier = customerTier != null ? customerTier : CustomerTier.STANDARD;
        installments = installments <= 0 ? 1 : installments;
        idempotencyKey = normalizeBlankToNull(idempotencyKey);
        pixKey = normalizeBlankToNull(pixKey);
        description = normalizeBlankToNull(description);
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return trimmed;
    }

    private static String normalizeBlankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
