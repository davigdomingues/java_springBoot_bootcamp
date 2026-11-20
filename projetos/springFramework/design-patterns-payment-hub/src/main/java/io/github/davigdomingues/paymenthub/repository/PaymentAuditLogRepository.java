package io.github.davigdomingues.paymenthub.repository;

import io.github.davigdomingues.paymenthub.domain.entity.PaymentAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório Spring Data JPA para trilha de auditoria das transações.
 */
public interface PaymentAuditLogRepository extends JpaRepository<PaymentAuditLog, Long> {

    List<PaymentAuditLog> findByTransactionCodeOrderByRecordedAtAsc(String transactionCode);
}
