package io.github.davigdomingues.paymenthub.pattern.adapter;

import io.github.davigdomingues.paymenthub.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * <strong>Padrão GoF: Adapter (Implementação Concreta)</strong>
 *
 * <p>Adapta o contrato moderno {@link ExternalSettlementPort} ao protocolo posicional
 * delimitado por pipe exigido por {@link LegacyMainframeClient}, convertendo valores
 * decimais para centavos, sanitizando delimitadores e interpretando códigos de retorno.</p>
 */
@Component
public class LegacyMainframeSettlementAdapter implements ExternalSettlementPort {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final LegacyMainframeClient mainframeClient;

    public LegacyMainframeSettlementAdapter(LegacyMainframeClient mainframeClient) {
        this.mainframeClient = Objects.requireNonNull(mainframeClient, "mainframeClient must not be null");
    }

    @Override
    public SettlementResponse settle(SettlementRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        long amountInCents = toCents(request.amount());

        String frame = String.join(
                "|",
                "SETTLE",
                sanitize(request.transactionCode()),
                Long.toString(amountInCents),
                sanitize(request.paymentMethod().name()),
                sanitize(request.payerDocument()),
                sanitize(request.payeeDocument())
        );

        String rawResponse = mainframeClient.transmitPipeFrame(frame);
        return parseProtocolResponse(rawResponse);
    }

    @Override
    public SettlementResponse refund(String externalAuthorizationCode, BigDecimal amount) {
        if (externalAuthorizationCode == null || externalAuthorizationCode.isBlank()) {
            throw new BusinessRuleException("External authorization code is required for refund");
        }
        Objects.requireNonNull(amount, "amount must not be null");
        long amountInCents = toCents(amount);

        String frame = String.join(
                "|",
                "REFUND",
                sanitize(externalAuthorizationCode),
                Long.toString(amountInCents)
        );

        String rawResponse = mainframeClient.transmitPipeFrame(frame);
        return parseProtocolResponse(rawResponse);
    }

    private long toCents(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_EVEN)
                .multiply(ONE_HUNDRED)
                .longValueExact();
    }

    private String sanitize(String field) {
        return field == null ? "" : field.replace("|", "_").trim();
    }

    private SettlementResponse parseProtocolResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new BusinessRuleException("Empty response received from legacy mainframe");
        }

        String[] parts = rawResponse.split("\\|", 3);
        String statusToken = parts[0];
        if ("OK".equals(statusToken) && parts.length == 3) {
            return new SettlementResponse(true, parts[1], parts[2], rawResponse);
        }

        String errorCode = parts.length >= 2 ? parts[1] : "UNKNOWN_ERROR";
        String errorMessage = parts.length == 3 ? parts[2] : rawResponse;
        throw new BusinessRuleException("Legacy mainframe settlement failed [" + errorCode + "]: " + errorMessage);
    }
}
