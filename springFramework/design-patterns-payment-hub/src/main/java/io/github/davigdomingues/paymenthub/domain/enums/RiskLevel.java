package io.github.davigdomingues.paymenthub.domain.enums;

/**
 * Classificação de risco atribuída pela cadeia de validação de compliance.
 *
 * <p><strong>Estrutura e impacto:</strong> deriva diretamente da pontuação acumulada em
 * {@code RiskAssessment} ({@code LOW} &lt; 30, {@code MEDIUM} &ge; 30, {@code HIGH} &ge; 60)
 * e determina o bloqueio da operação apenas quando atinge {@link #BLOCKED} (score 100),
 * acionando curto-circuito imediato da cadeia, persistência como {@code REJECTED} e
 * alerta via {@code FRAUD_ALERT_WEBHOOK}.</p>
 */
public enum RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    BLOCKED
}
