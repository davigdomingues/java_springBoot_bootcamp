package io.github.davigdomingues.paymenthub.pattern.decorator;

import java.util.Objects;

/**
 * <strong>Padrão GoF: Decorator (Base Decorator)</strong>
 *
 * <p>Mantém a referência ao próximo componente da pilha de execução para delegação
 * transparente antes ou depois do comportamento adicional.</p>
 */
public abstract class AbstractPaymentPipelineDecorator implements PaymentExecutionPipeline {

    protected final PaymentExecutionPipeline delegate;

    protected AbstractPaymentPipelineDecorator(PaymentExecutionPipeline delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate pipeline must not be null");
    }
}
