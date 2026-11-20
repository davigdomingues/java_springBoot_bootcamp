package io.github.davigdomingues.paymenthub.pattern.decorator;

import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;
import io.github.davigdomingues.paymenthub.domain.model.PaymentExecutionResult;
import io.github.davigdomingues.paymenthub.domain.model.RiskAssessment;
import io.github.davigdomingues.paymenthub.pattern.builder.PaymentReceiptBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * <strong>Padrão GoF: Decorator (Concrete Decorator - Telemetria de Execução)</strong>
 *
 * <p>Mensura o tempo total gasto pelo pipeline subjacente em milissegundos e registra
 * a métrica de latência no recibo final de execução.</p>
 */
public class ExecutionTelemetryDecorator extends AbstractPaymentPipelineDecorator {

    private static final Logger log = LoggerFactory.getLogger(ExecutionTelemetryDecorator.class);
    private static final String DECORATOR_NAME = "ExecutionTelemetryDecorator";

    public ExecutionTelemetryDecorator(PaymentExecutionPipeline delegate) {
        super(delegate);
    }

    @Override
    public PaymentExecutionResult execute(PaymentContext context, RiskAssessment riskAssessment) {
        long startedAtNanos = System.nanoTime();
        PaymentExecutionResult innerResult = delegate.execute(context, riskAssessment);
        long elapsedMs = Math.max(0L, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAtNanos));

        log.debug(
                "Payment pipeline executed transactionCode={} method={} elapsedMs={}",
                context.transactionCode(),
                context.paymentMethod(),
                elapsedMs
        );

        return PaymentReceiptBuilder.from(innerResult)
                .processingTimeMs(elapsedMs)
                .addDecoratorApplied(DECORATOR_NAME)
                .build();
    }
}
