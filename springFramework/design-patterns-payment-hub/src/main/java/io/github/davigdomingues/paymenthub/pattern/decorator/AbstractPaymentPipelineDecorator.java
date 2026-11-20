package io.github.davigdomingues.paymenthub.pattern.decorator;

import java.util.Objects;

/**
 * <strong>Padrão GoF: Decorator (Base Decorator)</strong>
 *
 * <p><strong>Estrutura e impacto:</strong> encapsula a referência imutável ao próximo elo
 * da pilha ({@code delegate}), garantindo que cada decorator concreto delegue a execução
 * e apenas envolva/enriqueça o resultado sem quebrar o contrato de {@link PaymentExecutionPipeline}.</p>
 */
public abstract class AbstractPaymentPipelineDecorator implements PaymentExecutionPipeline {

    protected final PaymentExecutionPipeline delegate;

    /**
     * Inicializa o decorator vinculando o componente ou decorator subjacente.
     *
     * @throws NullPointerException quando {@code delegate} for nulo
     */
    protected AbstractPaymentPipelineDecorator(PaymentExecutionPipeline delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate pipeline must not be null");
    }
}
