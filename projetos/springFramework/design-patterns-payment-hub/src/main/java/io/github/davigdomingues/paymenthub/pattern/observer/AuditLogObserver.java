package io.github.davigdomingues.paymenthub.pattern.observer;

import io.github.davigdomingues.paymenthub.domain.entity.PaymentAuditLog;
import io.github.davigdomingues.paymenthub.domain.model.PaymentProcessedEvent;
import io.github.davigdomingues.paymenthub.repository.PaymentAuditLogRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * <strong>Padrão GoF: Observer (Concrete Observer - Trilha de Auditoria)</strong>
 *
 * <p>Persiste um registro imutável em {@code payment_audit_logs} para cada evento
 * relevante de criação, rejeição ou estorno de pagamento.</p>
 */
@Component
public class AuditLogObserver implements PaymentEventObserver {

    private final PaymentAuditLogRepository auditLogRepository;

    public AuditLogObserver(PaymentAuditLogRepository auditLogRepository) {
        this.auditLogRepository = Objects.requireNonNull(auditLogRepository, "auditLogRepository must not be null");
    }

    @Override
    public String observerName() {
        return "AuditLogObserver";
    }

    @Override
    public void onPaymentEvent(PaymentProcessedEvent event) {
        PaymentAuditLog entry = new PaymentAuditLog(
                event.transactionCode(),
                event.eventType(),
                event.status(),
                event.riskLevel(),
                event.details(),
                event.occurredAt()
        );
        auditLogRepository.save(entry);
    }
}
