package io.github.davigdomingues.paymenthub.repository;

import io.github.davigdomingues.paymenthub.domain.entity.PaymentTransaction;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para consultas e persistência de transações.
 */
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByTransactionCode(String transactionCode);

    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);

    long countByPayerDocumentAndCreatedAtAfter(String payerDocument, Instant since);

    List<PaymentTransaction> findAllByOrderByCreatedAtDesc();

    List<PaymentTransaction> findByPaymentMethodOrderByCreatedAtDesc(PaymentMethod paymentMethod);

    List<PaymentTransaction> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    List<PaymentTransaction> findByPaymentMethodAndStatusOrderByCreatedAtDesc(
            PaymentMethod paymentMethod,
            PaymentStatus status
    );
}
