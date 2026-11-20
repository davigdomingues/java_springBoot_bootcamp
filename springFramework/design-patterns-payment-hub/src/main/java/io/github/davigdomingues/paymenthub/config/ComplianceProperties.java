package io.github.davigdomingues.paymenthub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.List;

/**
 * Propriedades externalizadas e imutáveis das regras de compliance e controle de velocidade.
 *
 * Estrutura e ponto de maior impacto: utiliza {@code @ConfigurationProperties} em um {@code record}
 * Java no lugar de {@code @Value} para garantir binding seguro de listas YAML
 * ({@code sanctioned-documents}) e validação defensiva centralizada no compact constructor,
 * fornecendo fallbacks operacionais caso alguma chave seja omitida no {@code application.yml}.
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
    /**
     * Normaliza limites e garante invariante
     * {@code velocityBlockThreshold >= velocityAlertThreshold}.
     */
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
        // Garante que o limiar de bloqueio nunca fique abaixo do limiar de alerta por erro de configuração
        velocityBlockThreshold = velocityBlockThreshold != null && velocityBlockThreshold >= velocityAlertThreshold
                ? velocityBlockThreshold : Math.max(5, velocityAlertThreshold);
    }
}
