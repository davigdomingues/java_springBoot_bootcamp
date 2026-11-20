package io.github.davigdomingues.paymenthub.pattern.decorator;

import io.github.davigdomingues.paymenthub.domain.enums.CustomerTier;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.domain.model.PaymentContext;
import io.github.davigdomingues.paymenthub.domain.model.PaymentExecutionResult;
import io.github.davigdomingues.paymenthub.domain.model.RiskAssessment;
import io.github.davigdomingues.paymenthub.pattern.adapter.LegacyMainframeClient;
import io.github.davigdomingues.paymenthub.pattern.adapter.LegacyMainframeSettlementAdapter;
import io.github.davigdomingues.paymenthub.pattern.factory.PaymentStrategyFactory;
import io.github.davigdomingues.paymenthub.pattern.strategy.BoletoPaymentStrategy;
import io.github.davigdomingues.paymenthub.pattern.strategy.CreditCardPaymentStrategy;
import io.github.davigdomingues.paymenthub.pattern.strategy.PixPaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários da composição de Decorators sobre {@link CorePaymentExecutionPipeline}.
 *
 * Valida a ordem de empilhamento ({@code ExecutionTelemetryDecorator} ->
 * {@code CashbackBenefitDecorator} -> {@code Core}), cálculo e teto de cashback por tier, latência
 * registrada e rastreabilidade em {@code appliedDecorators}.
 */
class PaymentExecutionDecoratorTest {

    private PaymentExecutionPipeline pipeline;

    @BeforeEach
    void setUp() {
        PaymentStrategyFactory factory = new PaymentStrategyFactory(List.of(
                new PixPaymentStrategy(),
                new CreditCardPaymentStrategy(),
                new BoletoPaymentStrategy()
        ));
        LegacyMainframeSettlementAdapter adapter = new LegacyMainframeSettlementAdapter(new LegacyMainframeClient());
        CorePaymentExecutionPipeline core = new CorePaymentExecutionPipeline(factory, adapter);
        this.pipeline = new ExecutionTelemetryDecorator(new CashbackBenefitDecorator(core));
    }

    @Test
    void shouldApplyCashbackAndTelemetryDecoratorsForVipPixPayment() {
        PaymentContext vipContext = context(PaymentMethod.PIX, CustomerTier.VIP, "1000.00", "vip@pix");

        PaymentExecutionResult result = pipeline.execute(vipContext, RiskAssessment.clean());

        assertThat(result.feeAmount()).isEqualByComparingTo("0.00");
        assertThat(result.cashbackAmount()).isEqualByComparingTo("20.00");
        assertThat(result.netAmount()).isEqualByComparingTo("1000.00");
        assertThat(result.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(result.processingTimeMs()).isGreaterThanOrEqualTo(0L);
        assertThat(result.appliedDecorators())
                .containsExactly("CashbackBenefitDecorator", "ExecutionTelemetryDecorator");
    }

    @Test
    void shouldRespectPremiumEligibilityAndStandardZeroCashback() {
        PaymentContext premiumEligiblePix = context(PaymentMethod.PIX, CustomerTier.PREMIUM, "100.00", "p@pix");
        PaymentContext premiumSmallCard = context(PaymentMethod.CREDIT_CARD, CustomerTier.PREMIUM, "150.00", null);
        PaymentContext premiumBigCard = context(PaymentMethod.CREDIT_CARD, CustomerTier.PREMIUM, "10000.00", null);
        PaymentContext standardPix = context(PaymentMethod.PIX, CustomerTier.STANDARD, "1000.00", "s@pix");

        assertThat(pipeline.execute(premiumEligiblePix, RiskAssessment.clean()).cashbackAmount())
                .isEqualByComparingTo("1.00");
        assertThat(pipeline.execute(premiumSmallCard, RiskAssessment.clean()).cashbackAmount())
                .isEqualByComparingTo("0.00");
        // 1% de 10.000 = 100.00, mas o teto PREMIUM é 50.00
        assertThat(pipeline.execute(premiumBigCard, RiskAssessment.clean()).cashbackAmount())
                .isEqualByComparingTo("50.00");
        assertThat(pipeline.execute(standardPix, RiskAssessment.clean()).cashbackAmount())
                .isEqualByComparingTo("0.00");
    }

    private PaymentContext context(PaymentMethod method, CustomerTier tier, String amount, String pixKey) {
        return new PaymentContext(
                "TXN-20261119-000010",
                null,
                "12345678901",
                "98765432100",
                new BigDecimal(amount),
                method,
                tier,
                1,
                pixKey,
                "Decorator test"
        );
    }
}
