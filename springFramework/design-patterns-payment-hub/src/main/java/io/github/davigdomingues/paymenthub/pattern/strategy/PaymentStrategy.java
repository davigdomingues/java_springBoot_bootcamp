package io.github.davigdomingues.paymenthub.pattern.strategy;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;

import java.math.BigDecimal;

/**
 * <strong>Padrão GoF: Strategy (Contrato)</strong>
 *
 * <p>
 * <strong>Estrutura e ponto de maior impacto:</strong> isola as regras de
 * validação
 * específica, precificação tarifária, status inicial e janela operacional de
 * liquidação
 * por meio de pagamento. Cada implementação Spring concreta é descoberta
 * automaticamente
 * pela {@code PaymentStrategyFactory} e consultada tanto na simulação (dry-run)
 * quanto
 * no núcleo do pipeline de execução.
 * </p>
 */
public interface PaymentStrategy {

    /**
     * Retorna o meio de pagamento atendido por esta estratégia (chave no
     * {@code EnumMap} da Factory).
     *
     * @return constante de {@link PaymentMethod}
     */
    PaymentMethod supportedMethod();

    /**
     * Valida invariantes específicas do meio de pagamento antes da etapa de
     * compliance,
     * evitando consultas desnecessárias ao banco ou liquidação para payloads
     * inválidos.
     *
     * @param context contexto imutável do pagamento
     * @throws io.github.davigdomingues.paymenthub.exception.BusinessRuleException quando
     *                                                                             uma
     *                                                                             regra
     *                                                                             semântica
     *                                                                             específica
     *                                                                             do
     *                                                                             meio
     *                                                                             de
     *                                                                             pagamento
     *                                                                             é
     *                                                                             violada
     */
    void validateSpecificRules(PaymentContext context);

    /**
     * Calcula a tarifa aplicada sobre a operação de acordo com valor, parcelas e
     * tier do cliente.
     *
     * @param context contexto imutável do pagamento
     * @return valor da tarifa com escala monetária de 2 casas decimais
     *         ({@code HALF_EVEN})
     */
    BigDecimal calculateFee(PaymentContext context);

    /**
     * Define o status inicial atribuído após liquidação bem-sucedida, impactando
     * diretamente
     * a elegibilidade imediata ({@code APPROVED}) vs compensação assíncrona
     * ({@code PENDING_SETTLEMENT}).
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
