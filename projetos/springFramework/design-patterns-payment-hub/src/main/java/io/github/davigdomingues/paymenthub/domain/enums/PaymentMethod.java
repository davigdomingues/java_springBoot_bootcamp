package io.github.davigdomingues.paymenthub.domain.enums;

/**
 * Meios de pagamento suportados pelo Payment Hub.
 *
 * <p>Cada constante direciona dinamicamente a resolução da estratégia concreta
 * no padrão Strategy/Factory.</p>
 */
public enum PaymentMethod {
    PIX,
    CREDIT_CARD,
    BOLETO
}
