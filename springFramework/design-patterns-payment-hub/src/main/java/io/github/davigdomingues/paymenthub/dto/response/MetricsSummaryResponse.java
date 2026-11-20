package io.github.davigdomingues.paymenthub.dto.response;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Snapshot imutável de métricas operacionais mantido pelo {@code MetricsCollectorObserver}.
 *
 * Estrutura e impacto: espelha contadores de desfecho, volumes financeiros agregados
 * ({@code APPROVED} e {@code PENDING_SETTLEMENT}) e mapas de distribuição por método e status
 * expostos em {@code GET /api/v1/metrics/summary}, mantendo isolamento contra mutação concorrente
 * dos acumuladores em memória.
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
