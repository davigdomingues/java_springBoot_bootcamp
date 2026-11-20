package io.github.davigdomingues.paymenthub.repository;

import io.github.davigdomingues.paymenthub.domain.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório Spring Data JPA para histórico de notificações despachadas.
 */
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByTransactionCodeOrderByDispatchedAtAsc(String transactionCode);
}
