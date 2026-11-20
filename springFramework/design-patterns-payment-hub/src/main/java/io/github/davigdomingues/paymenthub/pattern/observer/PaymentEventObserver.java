package io.github.davigdomingues.paymenthub.pattern.observer;

import io.github.davigdomingues.paymenthub.domain.model.PaymentProcessedEvent;

/**
 * <strong>Padrão GoF: Observer (Observer Interface)</strong>
 *
 * <p>
 * <strong>Estrutura e impacto:</strong> contrato reativo implementado por todos
 * os
 * assinantes do barramento de eventos ({@code AuditLogObserver},
 * {@code NotificationDispatchObserver} e {@code MetricsCollectorObserver}).
 * Desacopla o
 * {@code PaymentOrchestrationFacade} de efeitos colaterais pós-transação e
 * permite que o
 * {@link PaymentEventPublisher} isole exceções individuais sem abortar os
 * demais assinantes.
 * </p>
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
