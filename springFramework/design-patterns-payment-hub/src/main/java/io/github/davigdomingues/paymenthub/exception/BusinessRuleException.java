package io.github.davigdomingues.paymenthub.exception;

/**
 * Exceção lançada quando uma regra de negócio do pagamento ou estorno é violada.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
