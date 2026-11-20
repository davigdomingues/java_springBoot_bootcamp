package io.github.davigdomingues.paymenthub.exception;

import java.util.List;
import java.util.Objects;

/**
 * Exceção lançada quando a Chain of Responsibility de compliance bloqueia a transação.
 *
 * <p>O código da transação rejeitada e as violações detectadas são preservados para
 * retorno estruturado ao cliente e registro prévio em banco.</p>
 */
public class ComplianceViolationException extends RuntimeException {

    private final String transactionCode;
    private final List<String> violations;

    public ComplianceViolationException(String transactionCode, List<String> violations) {
        super("Payment blocked by compliance validation for transactionCode=" + transactionCode);
        this.transactionCode = Objects.requireNonNull(transactionCode, "transactionCode must not be null");
        this.violations = violations == null ? List.of() : List.copyOf(violations);
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public List<String> getViolations() {
        return violations;
    }
}
