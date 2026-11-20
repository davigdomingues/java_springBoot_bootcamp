package io.github.davigdomingues.paymenthub.pattern.adapter;

import java.math.BigDecimal;

/**
 * <strong>Padrão GoF: Adapter (Target Interface)</strong>
 *
 * <p>Contrato moderno esperado pelo núcleo do domínio para liquidação e estorno
 * financeiro, desacoplando o Payment Hub de protocolos externos legados.</p>
 */
public interface ExternalSettlementPort {

    /**
     * Liquida financeiramente uma operação no canal de compensação externo.
     *
     * @param request dados da liquidação em modelo de domínio
     * @return resposta traduzida e estruturada da liquidação
     */
    SettlementResponse settle(SettlementRequest request);

    /**
     * Solicita o estorno de uma liquidação previamente autorizada.
     *
     * @param externalAuthorizationCode código de autorização gerado na liquidação original
     * @param amount                    valor monetário a estornar
     * @return resposta traduzida da operação de estorno
     */
    SettlementResponse refund(String externalAuthorizationCode, BigDecimal amount);
}
