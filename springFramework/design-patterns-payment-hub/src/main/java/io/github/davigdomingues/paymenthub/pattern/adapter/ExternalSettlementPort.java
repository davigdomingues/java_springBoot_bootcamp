package io.github.davigdomingues.paymenthub.pattern.adapter;

import io.github.davigdomingues.paymenthub.exception.BusinessRuleException;

import java.math.BigDecimal;

/**
 * Padrão GoF: Adapter (Target Interface / Porta Hexagonal)
 *
 * Estrutura e ponto de maior impacto: define a fronteira anticorrupção entre o núcleo do domínio
 * (que opera com {@link BigDecimal}, enums tipados e registros imutáveis) e provedores externos
 * legados de compensação financeira. Impede que detalhes posicionais ou códigos brutos de mainframe
 * vazem para o {@code CorePaymentExecutionPipeline} ou para o {@code PaymentOrchestrationFacade}.
 */
public interface ExternalSettlementPort {

    /**
     * Liquida financeiramente uma operação no canal de compensação externo.
     *
     * @param request dados da liquidação em modelo de domínio
     * @return resposta traduzida e estruturada da liquidação
     * @throws BusinessRuleException quando o canal externo rejeitar a operação ou retornar frame inválido
     */
    SettlementResponse settle(SettlementRequest request);

    /**
     * Solicita o estorno de uma liquidação previamente autorizada.
     *
     * @param externalAuthorizationCode código de autorização gerado na liquidação original
     * @param amount valor monetário a estornar
     * @return resposta traduzida da operação de estorno
     * @throws BusinessRuleException quando o código de autorização estiver ausente ou o canal externo retornar erro
     */
    SettlementResponse refund(String externalAuthorizationCode, BigDecimal amount);
}
