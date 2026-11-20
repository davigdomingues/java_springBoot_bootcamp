package io.github.davigdomingues.paymenthub.pattern.chain;

import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;
import io.github.davigdomingues.paymenthub.domain.model.RiskAssessment;

import java.util.Set;

/**
 * Padrão GoF: Chain of Responsibility (Elo 1 - Sanções e Autotransferência)
 *
 * Estrutura e impacto: posicionado intencionalmente como primeiro elo da cadeia
 * ({@code PaymentPatternWiringConfig}) por ser puramente em memória e O(1). Bloqueia imediatamente
 * documentos sancionados (lista configurável via propriedades) ou autotransferências
 * ({@code payerDocument == payeeDocument}), acionando o curto-circuito da cadeia antes das
 * validações de limite e consulta de velocidade em banco.
 */
public class AccountSanctionValidationHandler extends ComplianceValidationHandler {

    private static final Set<String> DEFAULT_SANCTIONED_DOCUMENTS = Set.of("00000000000", "99999999999");

    private final Set<String> sanctionedDocuments;

    public AccountSanctionValidationHandler() {
        this(DEFAULT_SANCTIONED_DOCUMENTS);
    }

    public AccountSanctionValidationHandler(Set<String> sanctionedDocuments) {
        this.sanctionedDocuments = sanctionedDocuments == null
                ? DEFAULT_SANCTIONED_DOCUMENTS
                : Set.copyOf(sanctionedDocuments);
    }

    @Override
    protected RiskAssessment evaluate(PaymentContext context, RiskAssessment current) {
        if (sanctionedDocuments.contains(context.payerDocument())
                || sanctionedDocuments.contains(context.payeeDocument())) {
            return current.block("SANCTIONED_DOCUMENT");
        }
        if (context.payerDocument().equals(context.payeeDocument())) {
            return current.block("SELF_TRANSFER_NOT_ALLOWED");
        }
        return current;
    }
}
