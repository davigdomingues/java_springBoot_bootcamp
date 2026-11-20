package io.github.davigdomingues.paymenthub.pattern.adapter;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * <strong>Padrão GoF: Adapter (Adaptee - Cliente de Mainframe Legado)</strong>
 *
 * <p>Simula um conector bancário legado cujo protocolo exige mensagens posicionais
 * delimitadas por pipe ({@code |}) com valores em centavos.</p>
 */
@Component
public class LegacyMainframeClient {

    /**
     * Envia um comando bruto no formato posicional legado e devolve a resposta bruta.
     *
     * <p>Formatos aceitos:</p>
     * <ul>
     *     <li>{@code SETTLE|transactionCode|amountInCents|paymentMethod|payer|payee}</li>
     *     <li>{@code REFUND|authorizationCode|amountInCents}</li>
     * </ul>
     *
     * @param rawPipePayload payload delimitado por pipe
     * @return resposta no formato {@code OK|AUTH-...|CHANNEL} ou {@code ERR|CODE|MESSAGE}
     */
    public String transmitPipeFrame(String rawPipePayload) {
        if (rawPipePayload == null || rawPipePayload.isBlank()) {
            return "ERR|EMPTY_FRAME|Payload must not be blank";
        }

        String[] fields = rawPipePayload.split("\\|");
        String operation = fields[0];

        if ("SETTLE".equals(operation) && fields.length == 6) {
            long cents = parsePositiveCents(fields[2]);
            if (cents <= 0) {
                return "ERR|INVALID_AMOUNT|Amount in cents must be positive";
            }
            String authSuffix = shortDigest(rawPipePayload);
            String channel = resolveLegacyChannel(fields[3]);
            return "OK|AUTH-" + authSuffix + "|" + channel;
        }

        if ("REFUND".equals(operation) && fields.length == 3) {
            if (fields[1].isBlank()) {
                return "ERR|MISSING_AUTH|Authorization code is required for refund";
            }
            long cents = parsePositiveCents(fields[2]);
            if (cents <= 0) {
                return "ERR|INVALID_AMOUNT|Refund amount in cents must be positive";
            }
            String revSuffix = shortDigest(rawPipePayload);
            return "OK|REV-" + revSuffix + "|MAINFRAME_REVERSAL";
        }

        return "ERR|MALFORMED_FRAME|Unsupported legacy mainframe command";
    }

    private long parsePositiveCents(String rawCents) {
        try {
            return Long.parseLong(rawCents);
        } catch (NumberFormatException ex) {
            return -1L;
        }
    }

    private String resolveLegacyChannel(String paymentMethod) {
        return switch (paymentMethod) {
            case "PIX" -> "MAINFRAME_SPI";
            case "CREDIT_CARD" -> "MAINFRAME_ACQUIRER";
            case "BOLETO" -> "MAINFRAME_CIP";
            default -> "MAINFRAME_CORE";
        };
    }

    private String shortDigest(String seed) {
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8))
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();
    }
}
