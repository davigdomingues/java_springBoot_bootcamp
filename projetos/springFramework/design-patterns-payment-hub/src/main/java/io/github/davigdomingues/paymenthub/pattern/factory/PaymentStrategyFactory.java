package io.github.davigdomingues.paymenthub.pattern.factory;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.exception.UnsupportedPaymentMethodException;
import io.github.davigdomingues.paymenthub.pattern.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <strong>Padrão GoF: Factory Method / Registry</strong>
 *
 * <p>Recebe automaticamente todas as implementações de {@link PaymentStrategy} injetadas
 * pelo contêiner Spring e fabrica/resolve a estratégia apropriada via {@link EnumMap},
 * garantindo extensibilidade com respeito ao princípio Aberto-Fechado (OCP).</p>
 */
@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategies;

    public PaymentStrategyFactory(List<PaymentStrategy> strategyBeans) {
        Objects.requireNonNull(strategyBeans, "strategyBeans must not be null");
        Map<PaymentMethod, PaymentStrategy> registry = new EnumMap<>(PaymentMethod.class);
        for (PaymentStrategy strategy : strategyBeans) {
            PaymentMethod method = Objects.requireNonNull(
                    strategy.supportedMethod(),
                    "supportedMethod must not be null for " + strategy.getClass().getSimpleName()
            );
            PaymentStrategy previous = registry.put(method, strategy);
            if (previous != null) {
                throw new IllegalStateException("Duplicate PaymentStrategy registered for method: " + method);
            }
        }
        this.strategies = Collections.unmodifiableMap(registry);
    }

    /**
     * Resolve a estratégia correspondente ao meio de pagamento informado.
     *
     * @param method meio de pagamento desejado
     * @return instância concreta de {@link PaymentStrategy}
     */
    public PaymentStrategy getStrategy(PaymentMethod method) {
        if (method == null) {
            throw new UnsupportedPaymentMethodException("Payment method must not be null");
        }
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new UnsupportedPaymentMethodException("No strategy registered for payment method: " + method);
        }
        return strategy;
    }

    /**
     * Lista todos os meios de pagamento atualmente registrados na fábrica.
     *
     * @return conjunto imutável de métodos suportados
     */
    public Set<PaymentMethod> supportedMethods() {
        return strategies.keySet();
    }
}
