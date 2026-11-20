package io.github.davigdomingues.paymenthub.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/**
 * Registro de notificações disparadas pelo {@code NotificationDispatchObserver}
 * para canais como {@code EMAIL}, {@code PUSH} e {@code FRAUD_ALERT_WEBHOOK}.
 */
@Entity
@Table(name = "notification_logs")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_code", nullable = false, length = 40)
    private String transactionCode;

    @Column(name = "channel", nullable = false, length = 40)
    private String channel;

    @Column(name = "recipient", nullable = false, length = 120)
    private String recipient;

    @Column(name = "subject", nullable = false, length = 180)
    private String subject;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "dispatched_at", nullable = false, updatable = false)
    private Instant dispatchedAt;

    protected NotificationLog() {
    }

    public NotificationLog(
            String transactionCode,
            String channel,
            String recipient,
            String subject,
            String message,
            Instant dispatchedAt
    ) {
        this.transactionCode = Objects.requireNonNull(transactionCode, "transactionCode must not be null");
        this.channel = Objects.requireNonNull(channel, "channel must not be null");
        this.recipient = Objects.requireNonNull(recipient, "recipient must not be null");
        this.subject = Objects.requireNonNull(subject, "subject must not be null");
        this.message = message != null ? message : "";
        this.dispatchedAt = dispatchedAt != null ? dispatchedAt : Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public String getChannel() {
        return channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public Instant getDispatchedAt() {
        return dispatchedAt;
    }
}
