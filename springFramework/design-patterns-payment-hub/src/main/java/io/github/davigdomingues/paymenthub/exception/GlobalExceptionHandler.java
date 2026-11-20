package io.github.davigdomingues.paymenthub.exception;

import io.github.davigdomingues.paymenthub.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

/**
 * Interceptador global ({@link RestControllerAdvice}) responsável por traduzir exceções
 * de validação, domínio, compliance e falhas internas em respostas {@link ApiErrorResponse}.
 *
 * <p><strong>Matriz de mapeamento HTTP e impacto arquitetural:</strong></p>
 * <ul>
 *     <li>{@code 400 Bad Request}: {@link MethodArgumentNotValidException},
 *         {@link HttpMessageNotReadableException} e {@link MethodArgumentTypeMismatchException}
 *         (erros sintáticos ou de conversão antes da camada de negócio).</li>
 *     <li>{@code 404 Not Found}: {@link ResourceNotFoundException}.</li>
 *     <li>{@code 422 Unprocessable Entity}: {@link BusinessRuleException},
 *         {@link UnsupportedPaymentMethodException} e {@link ComplianceViolationException}
 *         (preservando a lista de violações no campo {@code details}).</li>
 *     <li>{@code 500 Internal Server Error}: fallback genérico para {@link Exception},
 *         registrando o stack trace completo apenas em log e devolvendo mensagem opaca.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Converte falhas de Bean Validation ({@code @Valid}) em HTTP 400 listando cada campo inválido.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "One or more request fields failed validation",
                request.getRequestURI(),
                details
        );
    }

    /**
     * Trata JSON malformado, enums inválidos ou parâmetros de query incompatíveis com HTTP 400.
     */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed Request",
                "Invalid request payload or parameter value",
                request.getRequestURI(),
                List.of(extractSafeMessage(ex))
        );
    }

    /**
     * Mapeia consultas a códigos de transação inexistentes para HTTP 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        );
    }

    /**
     * Mapeia violações de regras das estratégias, adaptador legado ou fábrica para HTTP 422.
     */
    @ExceptionHandler({BusinessRuleException.class, UnsupportedPaymentMethodException.class})
    public ResponseEntity<ApiErrorResponse> handleBusinessExceptions(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Business Rule Violation",
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        );
    }

    /**
     * Mapeia bloqueios da cadeia de compliance para HTTP 422 expondo o código rejeitado e flags.
     */
    @ExceptionHandler(ComplianceViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleComplianceViolation(
            ComplianceViolationException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Compliance Violation",
                ex.getMessage(),
                request.getRequestURI(),
                ex.getViolations()
        );
    }

    /**
     * Fallback de segurança para exceções não previstas, evitando vazamento de stack trace
     * ou detalhes internos de infraestrutura na resposta HTTP 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error while handling request uri={}", request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected internal error occurred",
                request.getRequestURI(),
                List.of()
        );
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    // Extrai a mensagem mais específica disponível sem expor stack trace ao consumidor da API
    private String extractSafeMessage(Exception ex) {
        Throwable root = ex.getCause() != null ? ex.getCause() : ex;
        return root.getMessage() != null ? root.getMessage() : "Invalid request payload";
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message,
            String path,
            List<String> details
    ) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                error,
                message,
                path,
                details
        );
        return ResponseEntity.status(status).body(body);
    }
}
