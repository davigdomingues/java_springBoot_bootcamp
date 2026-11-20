package io.github.davigdomingues.paymenthub.pattern.chain;

import io.github.davigdomingues.paymenthub.domain.enums.CustomerTier;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.RiskLevel;
import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;
import io.github.davigdomingues.paymenthub.domain.model.RiskAssessment;
import io.github.davigdomingues.paymenthub.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Testes unitários da cadeia {@link ComplianceValidationHandler}.
 *
 * <p>Verifica aprovação limpa, <strong>curto-circuito</strong> sem acesso ao repositório em
 * bloqueios determinísticos ({@code verifyNoInteractions}), acúmulo cumulativo de score/flags
 * entre elos (Valor + Velocidade) e bloqueio por teto de modalidade ou rajada.</p>
 */
@ExtendWith(MockitoExtension.class)
class ComplianceValidationChainTest {

    @Mock
    private PaymentTransactionRepository transactionRepository;

    private ComplianceValidationHandler chain;

    @BeforeEach
    void setUp() {
        ComplianceValidationHandler sanction = new AccountSanctionValidationHandler();
        ComplianceValidationHandler amountLimit = new AmountLimitValidationHandler();
        ComplianceValidationHandler velocity = new VelocityRiskValidationHandler(transactionRepository);
        sanction.linkWith(amountLimit).linkWith(velocity);
        this.chain = sanction;
    }

    @Test
    void shouldApproveCleanLowRiskTransaction() {
        when(transactionRepository.countByPayerDocumentAndCreatedAtAfter(eq("12345678901"), any()))
                .thenReturn(0L);

        RiskAssessment assessment = chain.validate(context("12345678901", "98765432100", "1500.00", PaymentMethod.PIX));

        assertThat(assessment.approved()).isTrue();
        assertThat(assessment.riskLevel()).isEqualTo(RiskLevel.LOW);
        assertThat(assessment.riskScore()).isZero();
        assertThat(assessment.flags()).isEmpty();
    }

    @Test
    void shouldShortCircuitChainWhenSanctionedDocumentIsDetected() {
        RiskAssessment assessment = chain.validate(context("00000000000", "98765432100", "100.00", PaymentMethod.PIX));

        assertThat(assessment.approved()).isFalse();
        assertThat(assessment.riskLevel()).isEqualTo(RiskLevel.BLOCKED);
        assertThat(assessment.riskScore()).isEqualTo(100);
        assertThat(assessment.flags()).containsExactly("SANCTIONED_DOCUMENT");
        // Confirma que o curto-circuito impediu chamada SQL ao elo de velocidade
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldBlockSelfTransfer() {
        RiskAssessment assessment = chain.validate(context("12345678901", "12345678901", "100.00", PaymentMethod.PIX));

        assertThat(assessment.approved()).isFalse();
        assertThat(assessment.riskLevel()).isEqualTo(RiskLevel.BLOCKED);
        assertThat(assessment.flags()).containsExactly("SELF_TRANSFER_NOT_ALLOWED");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldAccumulateRiskFlagsAcrossChainWithoutBlockingWhenWithinLimits() {
        when(transactionRepository.countByPayerDocumentAndCreatedAtAfter(eq("12345678901"), any()))
                .thenReturn(3L);

        RiskAssessment assessment = chain.validate(context("12345678901", "98765432100", "25000.00", PaymentMethod.PIX));

        assertThat(assessment.approved()).isTrue();
        // 35 pts (HIGH_VALUE_TRANSACTION) + 30 pts (ELEVATED_VELOCITY) = 65 pts => RiskLevel.HIGH
        assertThat(assessment.riskScore()).isEqualTo(65);
        assertThat(assessment.riskLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(assessment.flags()).containsExactly("HIGH_VALUE_TRANSACTION", "ELEVATED_VELOCITY");
    }

    @Test
    void shouldBlockWhenBoletoExceedsSpecificLimitOrGlobalLimit() {
        RiskAssessment boletoBlocked = chain.validate(
                context("12345678901", "98765432100", "55000.00", PaymentMethod.BOLETO)
        );
        assertThat(boletoBlocked.approved()).isFalse();
        assertThat(boletoBlocked.flags()).containsExactly("BOLETO_AMOUNT_LIMIT_EXCEEDED");

        RiskAssessment globalBlocked = chain.validate(
                context("12345678901", "98765432100", "150000.00", PaymentMethod.PIX)
        );
        assertThat(globalBlocked.approved()).isFalse();
        assertThat(globalBlocked.flags()).containsExactly("GLOBAL_AMOUNT_LIMIT_EXCEEDED");
    }

    @Test
    void shouldBlockWhenVelocityThresholdIsReached() {
        when(transactionRepository.countByPayerDocumentAndCreatedAtAfter(eq("12345678901"), any()))
                .thenReturn(5L);

        RiskAssessment assessment = chain.validate(context("12345678901", "98765432100", "25000.00", PaymentMethod.PIX));

        assertThat(assessment.approved()).isFalse();
        assertThat(assessment.riskLevel()).isEqualTo(RiskLevel.BLOCKED);
        assertThat(assessment.flags()).containsExactly("HIGH_VALUE_TRANSACTION", "VELOCITY_LIMIT_EXCEEDED");
    }

    private PaymentContext context(String payer, String payee, String amount, PaymentMethod method) {
        return new PaymentContext(
                "TXN-20261119-000001",
                null,
                payer,
                payee,
                new BigDecimal(amount),
                method,
                CustomerTier.STANDARD,
                1,
                method == PaymentMethod.PIX ? "pix@key" : null,
                "Compliance test"
        );
    }
}
