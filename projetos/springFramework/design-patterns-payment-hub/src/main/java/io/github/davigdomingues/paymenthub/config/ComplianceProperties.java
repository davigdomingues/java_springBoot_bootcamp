package io.github.davigdomingues.paymenthub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * Propriedades externalizadas das regras de compliance e controle de velocidade.
 */
@ConfigurationProperties(prefix = "payment-hub.compliance")
public record ComplianceProperties(
        List<String> sanctionedDocuments,
        BigDecimal maxGlobalAmount,
        BigDecimal maxBoletoAmount,
        BigDecimal highValueThreshold,
        Long velocityWindowMinutes,
        Integer velocityAlertThreshold,
        Integer velocityBlockThreshold
) {
    public ComplianceProperties {
        sanctionedDocuments = (sanctionedDocuments == null || sanctionedDocuments.isEmpty())
                ? List.of("00000000000", "99999999999")
                : List.copyOf(sanctionedDocuments);
        maxGlobalAmount = maxGlobalAmount != null ? maxGlobalAmount : new BigDecimal("100000.00");
        maxBoletoAmount = maxBoletoAmount != null ? maxBoletoAmount : new BigDecimal("50000.00");
        highValueThreshold = highValueThreshold != null ? highValueThreshold : new BigDecimal("20000.00");
        velocityWindowMinutes = velocityWindowMinutes != null && velocityWindowMinutes > 0
                ? velocityWindowMinutes : 5L;
        velocityAlertThreshold = velocityAlertThreshold != null && velocityAlertThreshold > 0
                ? velocityAlertThreshold : 3;
        velocityBlockThreshold = velocityBlockThreshold != null && velocityBlockThreshold >= velocityAlertThreshold
                ? velocityBlockThreshold : Math.max(5, velocityAlertThreshold);
    }
}
