package io.github.davigdomingues.paymenthub.pattern.strategy;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;

import java.math.BigDecimal;

/**
 * <strong>Padrão GoF: Strategy (Contrato)</strong>
 *
 * <p>Encapsula as regras de validação, cálculo de tarifa, status inicial e janela
 * de liquidação específicas de cada meio de pagamento suportado.</p>
 */
public interface PaymentStrategy {

    /**
     * Retorna o meio de pagamento atendido por esta estratégia.
     *
     * @return constante de {@link PaymentMethod}
     */
    PaymentMethod supportedMethod();

    /**
     * Valida invariantes específicas do meio de pagamento antes da etapa de compliance.
     *
     * @param context contexto imutável do pagamento
     */
    void validateSpecificRules(PaymentContext context);

    /**
     * Calcula a tarifa aplicada sobre a operação de acordo com valor, parcelas e tier do cliente.
     *
     * @param context contexto imutável do pagamento
     * @return valor da tarifa com escala monetária de 2 casas decimais
     */
    BigDecimal calculateFee(PaymentContext context);

    /**
     * Define o status inicial atribuído após liquidação bem-sucedida.
     *
     * @return status inicial da transação
     */
    PaymentStatus resolveInitialStatus();

    /**
     * Descreve a janela operacional de compensação/liquidação do meio de pagamento.
     *
     * @return descrição textual da janela de liquidação
     */
    String settlementWindowDescription();
}
