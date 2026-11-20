package io.github.davigdomingues.paymenthub.exception;

/**
 * Exceção lançada pela {@code PaymentStrategyFactory} quando o {@code PaymentMethod} informado é
 * nulo ou não possui implementação de {@code PaymentStrategy} registrada no {@code EnumMap}.
 *
 * Estrutura e impacto: garante falha explícita e controlada caso um novo valor de enum seja
 * adicionado sem o respectivo bean Spring de estratégia, sendo traduzida pelo
 * {@link GlobalExceptionHandler} em HTTP {@code 422 Unprocessable Entity}.
 */
public class UnsupportedPaymentMethodException extends RuntimeException {

    public UnsupportedPaymentMethodException(String message) {
        super(message);
    }
}
