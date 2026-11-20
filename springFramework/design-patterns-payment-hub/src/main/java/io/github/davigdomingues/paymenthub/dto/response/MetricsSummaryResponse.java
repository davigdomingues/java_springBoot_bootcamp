package io.github.davigdomingues.paymenthub.dto.response;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Resumo consolidado de métricas em tempo real mantido pelo {@code MetricsCollectorObserver}.
 */
public record MetricsSummaryResponse(
        long totalTransactions,
        long approvedCount,
        long pendingSettlementCount,
        long rejectedCount,
        long refundedCount,
        BigDecimal totalGrossVolume,
        BigDecimal totalFeesCollected,
        BigDecimal totalCashbackGranted,
        Map<PaymentMethod, Long> countByMethod,
        Map<PaymentStatus, Long> countByStatus
) {
}
