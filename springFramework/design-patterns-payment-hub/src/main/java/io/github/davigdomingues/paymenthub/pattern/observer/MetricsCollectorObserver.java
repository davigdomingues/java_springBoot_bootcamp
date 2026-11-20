package io.github.davigdomingues.paymenthub.pattern.observer;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.model.PaymentProcessedEvent;
import io.github.davigdomingues.paymenthub.dto.response.MetricsSummaryResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * <strong>Padrão GoF: Observer (Concrete Observer - Coletor de Métricas)</strong>
 *
 * <p>Mantém contadores e volumes financeiros consolidados em memória de forma
 * thread-safe para consultas rápidas de observabilidade.</p>
 */
@Component
public class MetricsCollectorObserver implements PaymentEventObserver {

    private static final String EVENT_PAYMENT_REFUNDED = "PAYMENT_REFUNDED";

    private long totalTransactions;
    private long approvedCount;
    private long pendingSettlementCount;
    private long rejectedCount;
    private long refundedCount;
    private BigDecimal totalGrossVolume = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
    private BigDecimal totalFeesCollected = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
    private BigDecimal totalCashbackGranted = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
    private final Map<PaymentMethod, Long> countByMethod = new EnumMap<>(PaymentMethod.class);
    private final Map<PaymentStatus, Long> countByStatus = new EnumMap<>(PaymentStatus.class);

    @Override
    public String observerName() {
        return "MetricsCollectorObserver";
    }

    @Override
    public synchronized void onPaymentEvent(PaymentProcessedEvent event) {
        if (EVENT_PAYMENT_REFUNDED.equals(event.eventType())) {
            refundedCount++;
            countByStatus.merge(PaymentStatus.REFUNDED, 1L, Long::sum);
            return;
        }

        totalTransactions++;
        countByMethod.merge(event.paymentMethod(), 1L, Long::sum);
        countByStatus.merge(event.status(), 1L, Long::sum);

        switch (event.status()) {
            case APPROVED -> {
                approvedCount++;
                accumulateFinancials(event);
            }
            case PENDING_SETTLEMENT -> {
                pendingSettlementCount++;
                accumulateFinancials(event);
            }
            case REJECTED -> rejectedCount++;
            case REFUNDED -> refundedCount++;
        }
    }

    /**
     * Retorna um snapshot imutável das métricas acumuladas até o momento.
     *
     * @return resumo consolidado das métricas de pagamento
     */
    public synchronized MetricsSummaryResponse getSummary() {
        return new MetricsSummaryResponse(
                totalTransactions,
                approvedCount,
                pendingSettlementCount,
                rejectedCount,
                refundedCount,
                totalGrossVolume.setScale(2, RoundingMode.HALF_EVEN),
                totalFeesCollected.setScale(2, RoundingMode.HALF_EVEN),
                totalCashbackGranted.setScale(2, RoundingMode.HALF_EVEN),
                Collections.unmodifiableMap(new EnumMap<>(countByMethod)),
                Collections.unmodifiableMap(new EnumMap<>(countByStatus))
        );
    }

    /**
     * Zera todos os contadores e acumuladores em memória (útil para isolamento de testes).
     */
    public synchronized void reset() {
        totalTransactions = 0L;
        approvedCount = 0L;
        pendingSettlementCount = 0L;
        rejectedCount = 0L;
        refundedCount = 0L;
        totalGrossVolume = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        totalFeesCollected = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        totalCashbackGranted = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
        countByMethod.clear();
        countByStatus.clear();
    }

    private void accumulateFinancials(PaymentProcessedEvent event) {
        if (event.grossAmount() != null) {
            totalGrossVolume = totalGrossVolume.add(event.grossAmount());
        }
        if (event.feeAmount() != null) {
            totalFeesCollected = totalFeesCollected.add(event.feeAmount());
        }
        if (event.cashbackAmount() != null) {
            totalCashbackGranted = totalCashbackGranted.add(event.cashbackAmount());
        }
    }
}
