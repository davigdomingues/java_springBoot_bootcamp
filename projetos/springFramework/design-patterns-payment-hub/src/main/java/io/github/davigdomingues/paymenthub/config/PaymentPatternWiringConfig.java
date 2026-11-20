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
 * Configuração central responsável pela montagem explícita das cadeias de
 * <strong>Chain of Responsibility</strong> (compliance) e da pilha de
 * <strong>Decorators</strong> do pipeline de execução.
 */
@Configuration
@EnableConfigurationProperties(ComplianceProperties.class)
public class PaymentPatternWiringConfig {

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

        sanctionHandler.linkWith(amountHandler).linkWith(velocityHandler);
        return sanctionHandler;
    }

    @Bean
    @Primary
    public PaymentExecutionPipeline paymentExecutionPipeline(CorePaymentExecutionPipeline corePipeline) {
        PaymentExecutionPipeline withCashback = new CashbackBenefitDecorator(corePipeline);
        return new ExecutionTelemetryDecorator(withCashback);
    }
}
