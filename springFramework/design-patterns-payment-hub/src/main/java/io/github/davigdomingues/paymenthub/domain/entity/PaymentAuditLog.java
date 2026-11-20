package io.github.davigdomingues.paymenthub.domain.entity;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.enums.RiskLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/**
 * Registro imutável de auditoria persistido pelo {@code AuditLogObserver}.
 */
@Entity
@Table(name = "payment_audit_logs")
public class PaymentAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_code", nullable = false, length = 40)
    private String transactionCode;

    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Column(name = "details", nullable = false, length = 500)
    private String details;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    protected PaymentAuditLog() {
    }

    public PaymentAuditLog(
            String transactionCode,
            String eventType,
            PaymentStatus status,
            RiskLevel riskLevel,
            String details,
            Instant recordedAt
    ) {
        this.transactionCode = Objects.requireNonNull(transactionCode, "transactionCode must not be null");
        this.eventType = Objects.requireNonNull(eventType, "eventType must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.riskLevel = Objects.requireNonNull(riskLevel, "riskLevel must not be null");
        this.details = details != null ? details : "";
        this.recordedAt = recordedAt != null ? recordedAt : Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public String getEventType() {
        return eventType;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public String getDetails() {
        return details;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}
