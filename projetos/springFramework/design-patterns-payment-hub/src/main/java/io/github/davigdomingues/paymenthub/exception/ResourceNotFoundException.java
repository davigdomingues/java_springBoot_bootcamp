package io.github.davigdomingues.paymenthub.exception;

/**
 * Exceção lançada quando o recurso consultado não existe na base de dados.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
