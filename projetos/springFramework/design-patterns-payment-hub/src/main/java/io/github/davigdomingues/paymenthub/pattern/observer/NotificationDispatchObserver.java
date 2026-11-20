package io.github.davigdomingues.paymenthub.pattern.observer;

import io.github.davigdomingues.paymenthub.domain.entity.NotificationLog;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.model.PaymentProcessedEvent;
import io.github.davigdomingues.paymenthub.repository.NotificationLogRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * <strong>Padrão GoF: Observer (Concrete Observer - Despacho de Notificações)</strong>
 *
 * <p>Registra notificações multicanal no banco conforme o desfecho da transação:
 * {@code PUSH} e {@code EMAIL} para transações aceitas ou estornadas, e
 * {@code EMAIL} + {@code FRAUD_ALERT_WEBHOOK} para bloqueios de compliance.</p>
 */
@Component
public class NotificationDispatchObserver implements PaymentEventObserver {

    private final NotificationLogRepository notificationLogRepository;

    public NotificationDispatchObserver(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = Objects.requireNonNull(
                notificationLogRepository,
                "notificationLogRepository must not be null"
        );
    }

    @Override
    public String observerName() {
        return "NotificationDispatchObserver";
    }

    @Override
    public void onPaymentEvent(PaymentProcessedEvent event) {
        List<NotificationLog> notifications = buildNotificationsFor(event);
        notificationLogRepository.saveAll(notifications);
    }

    private List<NotificationLog> buildNotificationsFor(PaymentProcessedEvent event) {
        if (event.status() == PaymentStatus.REJECTED) {
            return List.of(
                    new NotificationLog(
                            event.transactionCode(),
                            "EMAIL",
                            "payer:" + event.payerDocument(),
                            "Payment blocked by compliance review",
                            "Transaction " + event.transactionCode() + " was rejected. Details: " + event.details(),
                            event.occurredAt()
                    ),
                    new NotificationLog(
                            event.transactionCode(),
                            "FRAUD_ALERT_WEBHOOK",
                            "security-operations@paymenthub.local",
                            "Compliance block alert: " + event.transactionCode(),
                            "Blocked " + event.paymentMethod() + " payment of " + event.grossAmount()
                                    + " for payer " + event.payerDocument() + ". Reason: " + event.details(),
                            event.occurredAt()
                    )
            );
        }

        String subject = event.status() == PaymentStatus.REFUNDED
                ? "Payment refunded: " + event.transactionCode()
                : "Payment update (" + event.status() + "): " + event.transactionCode();

        String message = "Transaction " + event.transactionCode()
                + " via " + event.paymentMethod()
                + " amount " + event.grossAmount()
                + " status " + event.status();

        return List.of(
                new NotificationLog(
                        event.transactionCode(),
                        "PUSH",
                        "payer:" + event.payerDocument(),
                        subject,
                        message,
                        event.occurredAt()
                ),
                new NotificationLog(
                        event.transactionCode(),
                        "EMAIL",
                        "payee:" + event.payeeDocument(),
                        subject,
                        message,
                        event.occurredAt()
                )
        );
    }
}
