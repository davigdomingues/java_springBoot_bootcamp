package io.github.davigdomingues.paymenthub.pattern.factory;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.exception.UnsupportedPaymentMethodException;
import io.github.davigdomingues.paymenthub.pattern.strategy.BoletoPaymentStrategy;
import io.github.davigdomingues.paymenthub.pattern.strategy.CreditCardPaymentStrategy;
import io.github.davigdomingues.paymenthub.pattern.strategy.PixPaymentStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários da Factory {@link PaymentStrategyFactory}.
 *
 * Valida resolução O(1) via {@code EnumMap}, proteção contra método nulo ou ausente
 * ({@link UnsupportedPaymentMethodException}) e detecção fail-fast de registro duplicado
 * ({@link IllegalStateException}) na inicialização do contexto.
 */
class PaymentStrategyFactoryTest {

    @Test
    void shouldResolveRegisteredStrategiesByPaymentMethod() {
        PaymentStrategyFactory factory = new PaymentStrategyFactory(List.of(
                new PixPaymentStrategy(),
                new CreditCardPaymentStrategy(),
                new BoletoPaymentStrategy()
        ));

        assertThat(factory.getStrategy(PaymentMethod.PIX)).isInstanceOf(PixPaymentStrategy.class);
        assertThat(factory.getStrategy(PaymentMethod.CREDIT_CARD)).isInstanceOf(CreditCardPaymentStrategy.class);
        assertThat(factory.getStrategy(PaymentMethod.BOLETO)).isInstanceOf(BoletoPaymentStrategy.class);
        assertThat(factory.supportedMethods())
                .containsExactlyInAnyOrder(PaymentMethod.PIX, PaymentMethod.CREDIT_CARD, PaymentMethod.BOLETO);
    }

    @Test
    void shouldThrowWhenMethodIsNullOrUnregistered() {
        PaymentStrategyFactory factory = new PaymentStrategyFactory(List.of(new PixPaymentStrategy()));

        assertThatThrownBy(() -> factory.getStrategy(null))
                .isInstanceOf(UnsupportedPaymentMethodException.class);

        assertThatThrownBy(() -> factory.getStrategy(PaymentMethod.BOLETO))
                .isInstanceOf(UnsupportedPaymentMethodException.class)
                .hasMessageContaining("BOLETO");
    }

    @Test
    void shouldRejectDuplicateStrategiesForSamePaymentMethod() {
        assertThatThrownBy(() -> new PaymentStrategyFactory(List.of(
                new PixPaymentStrategy(),
                new PixPaymentStrategy()
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PIX");
    }
}
