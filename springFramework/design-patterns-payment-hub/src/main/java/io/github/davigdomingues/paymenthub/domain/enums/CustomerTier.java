package io.github.davigdomingues.paymenthub.domain.enums;

/**
 * Níveis de relacionamento do cliente no hub de pagamentos.
 *
 * <p><strong>Estrutura e impacto:</strong> parametriza simultaneamente a camada
 * {@code Strategy} (descontos progressivos e isenções tarifárias em PIX, Cartão e Boleto)
 * e o {@code CashbackBenefitDecorator} (alíquotas de 0%, 1% com teto de R$ 50 e 2% com
 * teto de R$ 100). Quando omitido na requisição, assume {@link #STANDARD} como fallback
 * seguro para evitar privilégios indevidos.</p>
 */
public enum CustomerTier {
    STANDARD,
    PREMIUM,
    VIP
}
