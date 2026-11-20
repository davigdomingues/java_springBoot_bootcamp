package io.github.davigdomingues.paymenthub.pattern.decorator;

import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;
import io.github.davigdomingues.paymenthub.domain.model.PaymentExecutionResult;
import io.github.davigdomingues.paymenthub.domain.model.RiskAssessment;

/**
 * Padrão GoF: Decorator (Component Interface)
 *
 * Estrutura e impacto: define o contrato único implementado tanto pelo componente núcleo
 * ({@code CorePaymentExecutionPipeline}) quanto pelos decorators da pilha
 * ({@code ExecutionTelemetryDecorator} -> {@code CashbackBenefitDecorator} -> {@code Core}). Essa
 * uniformidade permite compor benefícios e observabilidade técnica em torno da liquidação
 * financeira sem alterar ou acoplar o componente central.
 */
public interface PaymentExecutionPipeline {

    /**
     * Executa o processamento financeiro do pagamento após aprovação na etapa de compliance.
     *
     * @param context contexto imutável da operação
     * @param riskAssessment avaliação de risco aprovada pela Chain of Responsibility
     * @return resultado imutável consolidado
     */
    PaymentExecutionResult execute(PaymentContext context, RiskAssessment riskAssessment);
}
