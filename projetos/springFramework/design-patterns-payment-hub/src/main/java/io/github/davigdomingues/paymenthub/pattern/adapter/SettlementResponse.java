package io.github.davigdomingues.paymenthub.pattern.adapter;

/**
 * Resposta padronizada traduzida pelo Adapter a partir do protocolo legado.
 */
public record SettlementResponse(
        boolean accepted,
        String authorizationCode,
        String clearingChannel,
        String rawProtocolResponse
) {
}
