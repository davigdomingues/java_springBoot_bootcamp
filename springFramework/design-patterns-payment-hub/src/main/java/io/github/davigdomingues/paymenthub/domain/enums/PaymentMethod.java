package io.github.davigdomingues.paymenthub.domain.enums;

/**
 * Meios de pagamento suportados pelo Payment Hub.
 *
 * Estrutura e impacto: atua como chave discriminadora no {@code EnumMap} da
 * {@code PaymentStrategyFactory}, eliminando cadeias {@code if/else} na seleção de regras
 * tarifárias, validações específicas, status inicial e canal de liquidação no
 * {@code LegacyMainframeClient}. Métodos não suportados ou nulos resultam em
 * {@code UnsupportedPaymentMethodException} (HTTP 422).
 */
public enum PaymentMethod {
    PIX,
    CREDIT_CARD,
    BOLETO
}
