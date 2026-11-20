package io.github.davigdomingues.paymenthub.exception;

/**
 * Exceção lançada pelo {@code PaymentOrchestrationFacade} quando uma consulta de transação,
 * estorno, trilha de auditoria ou notificações referencia um {@code transactionCode} inexistente.
 *
 * Estrutura e impacto: padroniza a ausência de recursos em operações de leitura e ciclo de vida,
 * sendo convertida pelo {@link GlobalExceptionHandler} em resposta HTTP {@code 404 Not Found} com
 * payload {@code ApiErrorResponse}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
