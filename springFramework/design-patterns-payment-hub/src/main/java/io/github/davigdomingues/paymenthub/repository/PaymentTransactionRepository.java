package io.github.davigdomingues.paymenthub.repository;

import io.github.davigdomingues.paymenthub.domain.entity.PaymentTransaction;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para consultas e persistência de {@link PaymentTransaction}.
 *
 * <p><strong>Estrutura e ponto de maior impacto:</strong> além do CRUD padrão e listagens
 * ordenadas para a API REST, fornece duas consultas críticas para o comportamento do sistema:
 * {@link #findByIdempotencyKey(String)} (garantia de idempotência antes de acionar liquidação
 * ou consumir código do Singleton) e {@link #countByPayerDocumentAndCreatedAtAfter(String, Instant)}
 * (motor da janela deslizante no {@code VelocityRiskValidationHandler} da Chain of Responsibility).</p>
 */
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    /** Localiza transação pelo código único gerado pelo Singleton. */
    Optional<PaymentTransaction> findByTransactionCode(String transactionCode);

    /** Deduplica retentativas de pagamento evitando nova liquidação e consumo de sequência. */
    Optional<PaymentTransaction> findByIdempotencyKey(String idempotencyKey);

    /** Conta operações recentes de um pagador para controle de velocidade na Chain of Responsibility. */
    long countByPayerDocumentAndCreatedAtAfter(String payerDocument, Instant since);

    List<PaymentTransaction> findAllByOrderByCreatedAtDesc();

    List<PaymentTransaction> findByPaymentMethodOrderByCreatedAtDesc(PaymentMethod paymentMethod);

    List<PaymentTransaction> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    List<PaymentTransaction> findByPaymentMethodAndStatusOrderByCreatedAtDesc(
            PaymentMethod paymentMethod,
            PaymentStatus status
    );
}
