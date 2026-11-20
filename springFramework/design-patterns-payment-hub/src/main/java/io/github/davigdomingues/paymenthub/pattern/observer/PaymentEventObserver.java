package io.github.davigdomingues.paymenthub.pattern.observer;

import io.github.davigdomingues.paymenthub.domain.model.PaymentProcessedEvent;

/**
 * <strong>Padrão GoF: Observer (Observer Interface)</strong>
 *
 * <p>Contrato para assinantes reativos interessados em eventos do ciclo de vida
 * das transações de pagamento.</p>
 */
public interface PaymentEventObserver {

    /**
     * Identifica o observador para fins de diagnóstico e registro dinâmico.
     *
     * @return nome legível do observador
     */
    String observerName();

    /**
     * Reage a um evento de pagamento emitido pelo publisher.
     *
     * @param event payload imutável do evento
     */
    void onPaymentEvent(PaymentProcessedEvent event);
}
