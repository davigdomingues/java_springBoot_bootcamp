package io.github.davigdomingues.paymenthub.pattern.observer;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.enums.RiskLevel;
import io.github.davigdomingues.paymenthub.domain.model.PaymentProcessedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários do {@link PaymentEventPublisher}.
 *
 * <p>Verifica inscrição/remoção dinâmica, proteção contra assinatura duplicada via
 * {@code CopyOnWriteArrayList.addIfAbsent} e <strong>isolamento de falhas</strong> quando
 * um observador lança exceção em runtime, garantindo entrega contínua aos demais assinantes.</p>
 */
class PaymentEventPublisherTest {

    @Test
    void shouldSupportDynamicSubscriptionUnsubscriptionAndIsolateObserverFailures() {
        AtomicInteger healthyCalls = new AtomicInteger(0);
        AtomicInteger removedCalls = new AtomicInteger(0);

        PaymentEventObserver healthyObserver = new PaymentEventObserver() {
            @Override
            public String observerName() {
                return "HealthyObserver";
            }

            @Override
            public void onPaymentEvent(PaymentProcessedEvent event) {
                healthyCalls.incrementAndGet();
            }
        };

        PaymentEventObserver failingObserver = new PaymentEventObserver() {
            @Override
            public String observerName() {
                return "FailingObserver";
            }

            @Override
            public void onPaymentEvent(PaymentProcessedEvent event) {
                throw new IllegalStateException("Simulated observer outage");
            }
        };

        PaymentEventObserver removableObserver = new PaymentEventObserver() {
            @Override
            public String observerName() {
                return "RemovableObserver";
            }

            @Override
            public void onPaymentEvent(PaymentProcessedEvent event) {
                removedCalls.incrementAndGet();
            }
        };

        PaymentEventPublisher publisher = new PaymentEventPublisher(List.of(failingObserver, healthyObserver));
        publisher.subscribe(removableObserver);
        publisher.subscribe(healthyObserver); // não deve duplicar

        assertThat(publisher.registeredObservers())
                .containsExactly("FailingObserver", "HealthyObserver", "RemovableObserver");

        PaymentProcessedEvent event = sampleEvent();
        publisher.notifyObservers(event);

        assertThat(healthyCalls.get()).isEqualTo(1);
        assertThat(removedCalls.get()).isEqualTo(1);

        publisher.unsubscribe(removableObserver);
        publisher.notifyObservers(event);

        assertThat(healthyCalls.get()).isEqualTo(2);
        assertThat(removedCalls.get()).isEqualTo(1);
    }

    private PaymentProcessedEvent sampleEvent() {
        return new PaymentProcessedEvent(
                "TXN-20261119-000099",
                "PAYMENT_PROCESSED",
                PaymentMethod.PIX,
                PaymentStatus.APPROVED,
                new BigDecimal("250.00"),
                BigDecimal.ZERO,
                new BigDecimal("5.00"),
                "12345678901",
                "98765432100",
                RiskLevel.LOW,
                Instant.now(),
                "Unit test event"
        );
    }
}
