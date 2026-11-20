package io.github.davigdomingues.paymenthub.dto.response;

import io.github.davigdomingues.paymenthub.domain.enums.CustomerTier;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.enums.RiskLevel;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resposta da simulação prévia (dry-run) exibindo tarifas, cashback estimado,
 * janela de liquidação e análise de compliance sem persistir no banco.
 *
 * <p><strong>Estrutura e impacto:</strong> utiliza as mesmas regras de {@code PaymentStrategy},
 * {@code CashbackBenefitDecorator} e {@code ComplianceValidationHandler} do fluxo real para
 * antecipar ao cliente o {@code projectedStatus} (inclusive possível {@code REJECTED}) sem
 * acionar mainframe, Singleton ou observadores.</p>
 */
public record PaymentSimulationResponse(
        PaymentMethod paymentMethod,
        CustomerTier customerTier,
        int installments,
        BigDecimal grossAmount,
        BigDecimal feeAmount,
        BigDecimal estimatedCashback,
        BigDecimal netAmount,
        PaymentStatus projectedStatus,
        String settlementWindow,
        boolean complianceApproved,
        RiskLevel riskLevel,
        int riskScore,
        List<String> riskFlags
) {
    public PaymentSimulationResponse {
        riskFlags = riskFlags == null ? List.of() : List.copyOf(riskFlags);
    }
}
