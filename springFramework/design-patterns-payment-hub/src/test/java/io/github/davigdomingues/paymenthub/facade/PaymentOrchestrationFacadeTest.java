package io.github.davigdomingues.paymenthub.facade;

import io.github.davigdomingues.paymenthub.domain.entity.PaymentTransaction;
import io.github.davigdomingues.paymenthub.domain.enums.CustomerTier;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.enums.RiskLevel;
import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;
import io.github.davigdomingues.paymenthub.domain.model.PaymentExecutionResult;
import io.github.davigdomingues.paymenthub.domain.model.PaymentProcessedEvent;
import io.github.davigdomingues.paymenthub.domain.model.RiskAssessment;
import io.github.davigdomingues.paymenthub.dto.request.CreatePaymentRequest;
import io.github.davigdomingues.paymenthub.dto.response.PaymentResponse;
import io.github.davigdomingues.paymenthub.exception.BusinessRuleException;
import io.github.davigdomingues.paymenthub.exception.ComplianceViolationException;
import io.github.davigdomingues.paymenthub.pattern.adapter.ExternalSettlementPort;
import io.github.davigdomingues.paymenthub.pattern.adapter.SettlementResponse;
import io.github.davigdomingues.paymenthub.pattern.builder.PaymentReceiptBuilder;
import io.github.davigdomingues.paymenthub.pattern.chain.ComplianceValidationHandler;
import io.github.davigdomingues.paymenthub.pattern.decorator.PaymentExecutionPipeline;
import io.github.davigdomingues.paymenthub.pattern.facade.PaymentOrchestrationFacade;
import io.github.davigdomingues.paymenthub.pattern.factory.PaymentStrategyFactory;
import io.github.davigdomingues.paymenthub.pattern.observer.PaymentEventPublisher;
import io.github.davigdomingues.paymenthub.pattern.singleton.TransactionCodeGenerator;
import io.github.davigdomingues.paymenthub.pattern.strategy.PixPaymentStrategy;
import io.github.davigdomingues.paymenthub.repository.NotificationLogRepository;
import io.github.davigdomingues.paymenthub.repository.PaymentAuditLogRepository;
import io.github.davigdomingues.paymenthub.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentOrchestrationFacadeTest {

    @Mock
    private ComplianceValidationHandler complianceChain;

    @Mock
    private PaymentExecutionPipeline executionPipeline;

    @Mock
    private ExternalSettlementPort settlementPort;

    @Mock
    private PaymentEventPublisher eventPublisher;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private PaymentAuditLogRepository auditLogRepository;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    private PaymentOrchestrationFacade facade;

    @BeforeEach
    void setUp() {
        TransactionCodeGenerator.INSTANCE.resetSequence();
        PaymentStrategyFactory strategyFactory = new PaymentStrategyFactory(List.of(new PixPaymentStrategy()));
        this.facade = new PaymentOrchestrationFacade(
                strategyFactory,
                complianceChain,
                executionPipeline,
                settlementPort,
                eventPublisher,
                transactionRepository,
                auditLogRepository,
                notificationLogRepository
        );
    }

    @Test
    void shouldReturnExistingTransactionImmediatelyWhenIdempotencyKeyMatches() {
        PaymentContext context = new PaymentContext(
                "TXN-20261119-000001",
                "idem-123",
                "12345678901",
                "98765432100",
                new BigDecimal("100.00"),
                PaymentMethod.PIX,
                CustomerTier.VIP,
                1,
                "pix@key",
                "Existing payment"
        );
        PaymentExecutionResult existingResult = PaymentReceiptBuilder.builder()
                .transactionCode("TXN-20261119-000001")
                .paymentMethod(PaymentMethod.PIX)
                .grossAmount(new BigDecimal("100.00"))
                .feeAmount(BigDecimal.ZERO)
                .cashbackAmount(new BigDecimal("2.00"))
                .status(PaymentStatus.APPROVED)
                .externalAuthorizationCode("AUTH-EXISTING")
                .clearingChannel("MAINFRAME_SPI")
                .build();
        PaymentTransaction existingEntity = PaymentTransaction.fromExecutionResult(context, existingResult);

        when(transactionRepository.findByIdempotencyKey("idem-123")).thenReturn(Optional.of(existingEntity));

        PaymentResponse response = facade.processPayment(sampleRequest("idem-123"));

        assertThat(response.transactionCode()).isEqualTo("TXN-20261119-000001");
        assertThat(response.externalAuthorizationCode()).isEqualTo("AUTH-EXISTING");
        verifyNoInteractions(complianceChain, executionPipeline, eventPublisher);
    }

    @Test
    void shouldPersistRejectedTransactionPublishEventAndThrowWhenComplianceBlocks() {
        when(transactionRepository.findByIdempotencyKey("idem-block")).thenReturn(Optional.empty());
        when(complianceChain.validate(any())).thenReturn(RiskAssessment.blocked("SANCTIONED_DOCUMENT"));
        when(transactionRepository.save(any(PaymentTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> facade.processPayment(sampleRequest("idem-block")))
                .isInstanceOf(ComplianceViolationException.class)
                .hasMessageContaining("TXN-")
                .satisfies(ex -> {
                    ComplianceViolationException violation = (ComplianceViolationException) ex;
                    assertThat(violation.getViolations()).containsExactly("SANCTIONED_DOCUMENT");
                });

        ArgumentCaptor<PaymentTransaction> txCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(txCaptor.getValue().getRiskLevel()).isEqualTo(RiskLevel.BLOCKED);

        ArgumentCaptor<PaymentProcessedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentProcessedEvent.class);
        verify(eventPublisher).notifyObservers(eventCaptor.capture());
        assertThat(eventCaptor.getValue().eventType()).isEqualTo("PAYMENT_REJECTED");
        assertThat(eventCaptor.getValue().status()).isEqualTo(PaymentStatus.REJECTED);
        verifyNoInteractions(executionPipeline);
    }

    @Test
    void shouldRefundApprovedTransactionAndRejectAlreadyRefundedOrRejectedTransaction() {
        PaymentContext context = new PaymentContext(
                "TXN-20261119-000010",
                null,
                "12345678901",
                "98765432100",
                new BigDecimal("250.00"),
                PaymentMethod.PIX,
                CustomerTier.STANDARD,
                1,
                "pix@key",
                "Refundable"
        );
        PaymentExecutionResult result = PaymentReceiptBuilder.builder()
                .transactionCode("TXN-20261119-000010")
                .paymentMethod(PaymentMethod.PIX)
                .grossAmount(new BigDecimal("250.00"))
                .feeAmount(BigDecimal.ZERO)
                .status(PaymentStatus.APPROVED)
                .externalAuthorizationCode("AUTH-ORIG")
                .clearingChannel("MAINFRAME_SPI")
                .build();
        PaymentTransaction entity = PaymentTransaction.fromExecutionResult(context, result);

        when(transactionRepository.findByTransactionCode("TXN-20261119-000010")).thenReturn(Optional.of(entity));
        when(settlementPort.refund("AUTH-ORIG", new BigDecimal("250.00")))
                .thenReturn(new SettlementResponse(true, "REV-123", "MAINFRAME_REVERSAL", "OK|REV-123|MAINFRAME_REVERSAL"));
        when(transactionRepository.save(any(PaymentTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse refunded = facade.refundPayment("TXN-20261119-000010");

        assertThat(refunded.status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(refunded.externalAuthorizationCode()).isEqualTo("REV-123");
        verify(eventPublisher).notifyObservers(any(PaymentProcessedEvent.class));

        assertThatThrownBy(() -> facade.refundPayment("TXN-20261119-000010"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("REFUNDED");
    }

    private CreatePaymentRequest sampleRequest(String idempotencyKey) {
        return new CreatePaymentRequest(
                "12345678901",
                "98765432100",
                new BigDecimal("100.00"),
                PaymentMethod.PIX,
                CustomerTier.VIP,
                1,
                "pix@key",
                idempotencyKey,
                "Test payment"
        );
    }
}
