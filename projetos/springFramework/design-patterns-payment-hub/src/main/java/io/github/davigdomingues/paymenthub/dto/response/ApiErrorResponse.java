package io.github.davigdomingues.paymenthub.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Payload padronizado de erro retornado pelos handlers globais da API REST.
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
