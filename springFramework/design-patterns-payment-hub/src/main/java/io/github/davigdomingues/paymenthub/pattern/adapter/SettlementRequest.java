package io.github.davigdomingues.paymenthub.pattern.adapter;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Requisição moderna e tipada enviada à porta de liquidação externa ({@link ExternalSettlementPort}).
 *
 * <p><strong>Estrutura e impacto:</strong> atua como contrato de entrada da fronteira
 * anticorrupção do Adapter, assegurando que nenhum campo identificador ou monetário nulo
 * alcance o tradutor de protocolo legado.</p>
 */
public record SettlementRequest(
        String transactionCode,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String payerDocument,
        String payeeDocument
) {
    /**
     * Valida os campos obrigatórios antes da serialização para o canal de compensação.
     *
     * @throws NullPointerException quando qualquer atributo do comando de liquidação for nulo
     */
    public SettlementRequest {
        Objects.requireNonNull(transactionCode, "transactionCode must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(paymentMethod, "paymentMethod must not be null");
        Objects.requireNonNull(payerDocument, "payerDocument must not be null");
        Objects.requireNonNull(payeeDocument, "payeeDocument must not be null");
    }
}
