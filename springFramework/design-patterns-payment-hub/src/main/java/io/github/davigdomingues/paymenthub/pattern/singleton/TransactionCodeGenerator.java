package io.github.davigdomingues.paymenthub.pattern.singleton;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <strong>Padrão GoF: Singleton (Enum Thread-Safe)</strong>
 *
 * <p>Garante uma instância única e segura contra serialização/reflexão para geração
 * monotônica de códigos de transação no formato {@code TXN-YYYYMMDD-000001}.</p>
 */
public enum TransactionCodeGenerator {
    INSTANCE;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final AtomicLong sequence = new AtomicLong(0);

    /**
     * Gera o próximo código único de transação em UTC de forma atômica.
     *
     * @return código formatado no padrão {@code TXN-YYYYMMDD-NNNNNN}
     */
    public String nextCode() {
        String datePart = LocalDate.now(ZoneOffset.UTC).format(DATE_FORMATTER);
        long nextNumber = sequence.incrementAndGet();
        return String.format("TXN-%s-%06d", datePart, nextNumber);
    }

    /**
     * Consulta o valor atual do contador sequencial.
     *
     * @return valor atual da sequência
     */
    public long currentSequence() {
        return sequence.get();
    }

    /**
     * Reinicia o contador sequencial (útil principalmente em testes automatizados).
     */
    public void resetSequence() {
        sequence.set(0);
    }
}
