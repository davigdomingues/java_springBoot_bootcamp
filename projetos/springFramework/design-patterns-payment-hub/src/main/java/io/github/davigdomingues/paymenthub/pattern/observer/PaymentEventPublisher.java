package io.github.davigdomingues.paymenthub.pattern.observer;

import io.github.davigdomingues.paymenthub.domain.model.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <strong>Padrão GoF: Observer (Subject / Event Publisher)</strong>
 *
 * <p>Gerencia dinamicamente os observadores inscritos e propaga eventos de pagamento
 * isolando eventuais falhas individuais para preservar a resiliência do fluxo.</p>
 */
@Component
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final CopyOnWriteArrayList<PaymentEventObserver> observers;

    public PaymentEventPublisher(List<PaymentEventObserver> observerBeans) {
        this.observers = new CopyOnWriteArrayList<>();
        if (observerBeans != null) {
            observerBeans.forEach(this::subscribe);
        }
    }

    /**
     * Inscreve um novo observador caso ainda não esteja registrado.
     *
     * @param observer instância do observador
     */
    public void subscribe(PaymentEventObserver observer) {
        Objects.requireNonNull(observer, "observer must not be null");
        observers.addIfAbsent(observer);
    }

    /**
     * Remove um observador da lista de assinantes.
     *
     * @param observer instância do observador a remover
     */
    public void unsubscribe(PaymentEventObserver observer) {
        if (observer != null) {
            observers.remove(observer);
        }
    }

    /**
     * Notifica todos os observadores inscritos sobre um novo evento de pagamento.
     *
     * @param event evento imutável ocorrido
     */
    public void notifyObservers(PaymentProcessedEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        for (PaymentEventObserver observer : observers) {
            try {
                observer.onPaymentEvent(event);
            } catch (RuntimeException ex) {
                log.error(
                        "Observer failure observer={} transactionCode={} eventType={}",
                        observer.observerName(),
                        event.transactionCode(),
                        event.eventType(),
                        ex
                );
            }
        }
    }

    /**
     * Lista os nomes dos observadores atualmente inscritos.
     *
     * @return lista imutável de nomes de observadores
     */
    public List<String> registeredObservers() {
        return observers.stream()
                .map(PaymentEventObserver::observerName)
                .toList();
    }
}
