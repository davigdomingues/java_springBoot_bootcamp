package io.github.davigdomingues.paymenthub.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Payload padronizado de erro retornado pelos handlers do {@code GlobalExceptionHandler}.
 *
 * Estrutura e impacto: unifica a resposta de falhas {@code 400}, {@code 404}, {@code 422} e
 * {@code 500} sem expor detalhes internos da JVM. Realiza cópia defensiva de {@code details} para
 * transportar campos inválidos (HTTP 400) ou flags de compliance (HTTP 422).
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public ApiErrorResponse {
        timestamp = timestamp != null ? timestamp : Instant.now();
        details = details == null ? List.of() : List.copyOf(details);
    }
}
