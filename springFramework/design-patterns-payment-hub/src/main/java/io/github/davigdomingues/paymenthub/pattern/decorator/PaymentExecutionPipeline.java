package io.github.davigdomingues.paymenthub.pattern.decorator;

import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;
import io.github.davigdomingues.paymenthub.domain.model.PaymentExecutionResult;
import io.github.davigdomingues.paymenthub.domain.model.RiskAssessment;

/**
 * <strong>Padrão GoF: Decorator (Component Interface)</strong>
 *
 * <p>Contrato do pipeline de execução de pagamentos que permite compor dinamicamente
 * responsabilidades adicionais (benefícios, telemetria, auditoria técnica) em torno
 * do núcleo de liquidação.</p>
 */
public interface PaymentExecutionPipeline {

    /**
     * Executa o processamento financeiro do pagamento após aprovação na etapa de compliance.
     *
     * @param context        contexto imutável da operação
     * @param riskAssessment avaliação de risco aprovada pela Chain of Responsibility
     * @return resultado imutável consolidado
     */
    PaymentExecutionResult execute(PaymentContext context, RiskAssessment riskAssessment);
}
