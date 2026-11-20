package io.github.davigdomingues.paymenthub.repository;

import io.github.davigdomingues.paymenthub.domain.entity.PaymentAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório Spring Data JPA para consulta e gravação da trilha de auditoria.
 *
 * <p>
 * <strong>Estrutura e impacto:</strong> utilizado em escrita pelo
 * {@code AuditLogObserver}
 * e em leitura pelo {@code PaymentOrchestrationFacade}, garantindo ordenação
 * cronológica
 * ascendente ({@code OrderByRecordedAtAsc}) para reconstrução fiel da linha do
 * tempo da transação.
 * </p>
 */
public interface PaymentAuditLogRepository extends JpaRepository<PaymentAuditLog, Long> {

    /**
     * Recupera todos os eventos auditados de uma transação em ordem cronológica
     * crescente.
     */
    List<PaymentAuditLog> findByTransactionCodeOrderByRecordedAtAsc(String transactionCode);
}
