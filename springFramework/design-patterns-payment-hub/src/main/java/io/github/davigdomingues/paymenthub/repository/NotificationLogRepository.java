package io.github.davigdomingues.paymenthub.repository;

import io.github.davigdomingues.paymenthub.domain.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório Spring Data JPA para histórico de notificações despachadas.
 *
 * <p><strong>Estrutura e impacto:</strong> alimentado pelo {@code NotificationDispatchObserver}
 * e consultado pelo endpoint de observabilidade de notificações via {@code PaymentOrchestrationFacade},
 * preservando a ordem cronológica de disparo por transação.</p>
 */
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    /** Recupera notificações emitidas para uma transação em ordem cronológica crescente. */
    List<NotificationLog> findByTransactionCodeOrderByDispatchedAtAsc(String transactionCode);
}
