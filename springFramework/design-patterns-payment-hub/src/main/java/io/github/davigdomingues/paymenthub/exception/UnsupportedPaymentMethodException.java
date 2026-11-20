package io.github.davigdomingues.paymenthub.exception;

/**
 * Exceção lançada pela Factory quando um meio de pagamento solicitado não possui estratégia registrada.
 */
public class UnsupportedPaymentMethodException extends RuntimeException {

    public UnsupportedPaymentMethodException(String message) {
        super(message);
    }
}
