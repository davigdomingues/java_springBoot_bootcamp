package io.github.davigdomingues.paymenthub.pattern.singleton;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Padrão GoF: Singleton (Enum Thread-Safe)
 *
 * Estrutura e ponto de maior impacto: implementa o padrão Singleton via {@code enum INSTANCE}
 * (Effective Java, Item 3), garantindo proteção nativa da JVM contra múltiplas instâncias por
 * serialização ou ataque por reflexão. Utiliza {@link AtomicLong} lock-free e data em UTC para
 * emitir identificadores únicos monotônicos no formato {@code TXN-YYYYMMDD-NNNNNN}, acionado apenas
 * após a verificação de idempotência no Facade para não desperdiçar sequência em retentativas.
 */
public enum TransactionCodeGenerator {
    INSTANCE;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    // Incremento atômico lock-free evita contenção sob concorrência sem synchronized global
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
     * Reinicia o contador sequencial para zero.
     *
     * Atenção: uso restrito ao isolamento entre testes automatizados; em produção o reset pode
     * colidir com códigos já persistidos pela restrição {@code unique}.
     */
    public void resetSequence() {
        sequence.set(0);
    }
}
