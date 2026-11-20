package io.github.davigdomingues.paymenthub.exception;

/**
 * Exceção de domínio lançada quando uma regra semântica de pagamento, liquidação ou estorno é
 * violada após a passagem pela validação sintática de DTO.
 *
 * Estrutura e impacto: disparada principalmente pelas implementações de {@code PaymentStrategy}
 * (chave PIX ausente, parcelamento inválido, boleto abaixo do mínimo), pelo
 * {@code LegacyMainframeSettlementAdapter} (respostas {@code ERR|...} do canal legado) e pelo
 * {@code PaymentOrchestrationFacade} (tentativa de estorno sobre transação {@code REJECTED} ou já
 * {@code REFUNDED}). Mapeada pelo {@link GlobalExceptionHandler} para HTTP
 * {@code 422 Unprocessable Entity}.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
