package io.github.davigdomingues.paymenthub.config;

import io.github.davigdomingues.paymenthub.pattern.chain.AccountSanctionValidationHandler;
import io.github.davigdomingues.paymenthub.pattern.chain.AmountLimitValidationHandler;
import io.github.davigdomingues.paymenthub.pattern.chain.ComplianceValidationHandler;
import io.github.davigdomingues.paymenthub.pattern.chain.VelocityRiskValidationHandler;
import io.github.davigdomingues.paymenthub.pattern.decorator.CashbackBenefitDecorator;
import io.github.davigdomingues.paymenthub.pattern.decorator.CorePaymentExecutionPipeline;
import io.github.davigdomingues.paymenthub.pattern.decorator.ExecutionTelemetryDecorator;
import io.github.davigdomingues.paymenthub.pattern.decorator.PaymentExecutionPipeline;
import io.github.davigdomingues.paymenthub.repository.PaymentTransactionRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuração central responsável pelo wiring explícito da Chain of Responsibility (compliance) e
 * da pilha de Decorators do pipeline de execução.
 *
 * Pontos de maior impacto arquitetural:
 * - Ordem da Chain: encadeia validadores em memória O(1) primeiro ({@code Sanction} ->
 *   {@code AmountLimit}) e consulta em banco por último ({@code VelocityRisk}), maximizando o ganho
 *   do curto-circuito.
 * - Desambiguação por {@code @Primary}: como {@link CorePaymentExecutionPipeline} também é um bean
 *   {@code @Component} do tipo {@link PaymentExecutionPipeline}, a anotação {@code @Primary}
 *   garante que o {@code PaymentOrchestrationFacade} receba sempre a pilha decorada completa
 *   ({@code Telemetry -> Cashback -> Core}) sem exigir {@code @Qualifier}.
 */
@Configuration
@EnableConfigurationProperties(ComplianceProperties.class)
public class PaymentPatternWiringConfig {

    /**
     * Monta e expõe o elo inicial da cadeia de compliance configurada por propriedades.
     */
    @Bean
    public ComplianceValidationHandler complianceValidationChain(
            PaymentTransactionRepository transactionRepository,
            ComplianceProperties properties
    ) {
        Set<String> normalizedSanctions = properties.sanctionedDocuments().stream()
                .map(String::trim)
                .filter(doc -> !doc.isEmpty())
                .collect(Collectors.toUnmodifiableSet());

        ComplianceValidationHandler sanctionHandler = new AccountSanctionValidationHandler(normalizedSanctions);
        ComplianceValidationHandler amountHandler = new AmountLimitValidationHandler(
                properties.maxGlobalAmount(),
                properties.maxBoletoAmount(),
                properties.highValueThreshold()
        );
        ComplianceValidationHandler velocityHandler = new VelocityRiskValidationHandler(
                transactionRepository,
                properties.velocityWindowMinutes(),
                properties.velocityAlertThreshold(),
                properties.velocityBlockThreshold()
        );

        // Ordem intencional: verificações determinísticas em memória antecedem consulta SQL de velocidade
        sanctionHandler.linkWith(amountHandler).linkWith(velocityHandler);
        return sanctionHandler;
    }

    /**
     * Compõe a pilha de execução envolvendo o núcleo com cashback e telemetria de latência.
     */
    @Bean
    @Primary
    public PaymentExecutionPipeline paymentExecutionPipeline(CorePaymentExecutionPipeline corePipeline) {
        // ExecutionTelemetryDecorator envolve CashbackBenefitDecorator para medir o pipeline completo
        PaymentExecutionPipeline withCashback = new CashbackBenefitDecorator(corePipeline);
        return new ExecutionTelemetryDecorator(withCashback);
    }
}
