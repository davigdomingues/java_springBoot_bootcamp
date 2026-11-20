package io.github.davigdomingues.paymenthub.pattern.chain;

import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;
import io.github.davigdomingues.paymenthub.domain.model.RiskAssessment;

import java.util.Objects;

/**
 * <strong>Padrão GoF: Chain of Responsibility (Handler Base)</strong>
 *
 * <p>Define a estrutura encadeada de validadores de compliance e antifraude.
 * Cada elo avalia o contexto de pagamento, acumula pontuação/flags de risco
 * e interrompe a cadeia imediatamente caso detecte uma violação bloqueante.</p>
 */
public abstract class ComplianceValidationHandler {

    private ComplianceValidationHandler next;

    /**
     * Encadeia o próximo validador após este elo.
     *
     * @param next próximo handler da cadeia
     * @return o próprio {@code next}, permitindo encadeamento fluente {@code a.linkWith(b).linkWith(c)}
     */
    public ComplianceValidationHandler linkWith(ComplianceValidationHandler next) {
        this.next = Objects.requireNonNull(next, "next handler must not be null");
        return next;
    }

    /**
     * Inicia a execução da cadeia a partir de uma avaliação de risco limpa.
     *
     * @param context contexto imutável do pagamento
     * @return avaliação consolidada da cadeia
     */
    public RiskAssessment validate(PaymentContext context) {
        return validate(context, RiskAssessment.clean());
    }

    /**
     * Executa este elo e propaga a avaliação acumulada para o próximo elo
     * apenas enquanto a transação permanecer aprovada.
     *
     * @param context contexto imutável do pagamento
     * @param current avaliação acumulada pelos elos anteriores
     * @return avaliação atualizada
     */
    public final RiskAssessment validate(PaymentContext context, RiskAssessment current) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(current, "current RiskAssessment must not be null");

        RiskAssessment updated = evaluate(context, current);
        if (!updated.approved() || next == null) {
            return updated;
        }
        return next.validate(context, updated);
    }

    /**
     * Implementa a regra específica de compliance deste elo da cadeia.
     *
     * @param context contexto do pagamento
     * @param current avaliação acumulada até o momento
     * @return nova avaliação resultante da análise deste elo
     */
    protected abstract RiskAssessment evaluate(PaymentContext context, RiskAssessment current);
}
