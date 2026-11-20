package io.github.davigdomingues.paymenthub.pattern.adapter;

/**
 * Resposta padronizada traduzida pelo Adapter a partir do protocolo posicional legado.
 *
 * <p><strong>Estrutura e impacto:</strong> isola o código de autorização ({@code AUTH-...}
 * ou {@code REV-...}) e o canal de compensação identificados no mainframe, preservando o
 * {@code rawProtocolResponse} para eventual diagnóstico técnico sem acoplar o domínio ao formato bruto.</p>
 */
public record SettlementResponse(
        boolean accepted,
        String authorizationCode,
        String clearingChannel,
        String rawProtocolResponse
) {
}
