package io.github.davigdomingues.paymenthub.domain.enums;

/**
 * Estados possíveis do ciclo de vida de uma transação no Payment Hub.
 *
 * Máquina de estados e impacto: {@code APPROVED} (PIX e Cartão) e {@code PENDING_SETTLEMENT}
 * (Boleto) são os únicos estados elegíveis para estorno posterior; tentativas de estorno sobre
 * transações {@code REJECTED} ou já {@code REFUNDED} disparam {@code BusinessRuleException} (HTTP
 * 422). Transações bloqueadas pela Chain of Responsibility nascem diretamente em {@code REJECTED} e
 * são preservadas em banco via {@code noRollbackFor}.
 */
public enum PaymentStatus {
    APPROVED,
    PENDING_SETTLEMENT,
    REJECTED,
    REFUNDED
}
