package io.github.davigdomingues.paymenthub.pattern.adapter;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários do Adapter {@link LegacyMainframeSettlementAdapter}.
 *
 * Valida serialização decimal -> centavos, sanitização de delimitadores {@code |} contra corrupção
 * posicional do frame, comandos {@code SETTLE}/{@code REFUND} e tradução de respostas
 * {@code ERR|...} para {@link BusinessRuleException}.
 */
@ExtendWith(MockitoExtension.class)
class LegacyMainframeSettlementAdapterTest {

    @Test
    void shouldTranslateSettlementRequestToPipeCentsFrameAndParseResponse() {
        LegacyMainframeClient spyClient = Mockito.spy(new LegacyMainframeClient());
        LegacyMainframeSettlementAdapter adapter = new LegacyMainframeSettlementAdapter(spyClient);

        // Código contém pipes intencionais para comprovar sanitização antes do envio ao mainframe
        SettlementResponse response = adapter.settle(new SettlementRequest(
                "TXN|20261119|000001",
                new BigDecimal("123.45"),
                PaymentMethod.PIX,
                "12345678901",
                "98765432100"
        ));

        verify(spyClient).transmitPipeFrame("SETTLE|TXN_20261119_000001|12345|PIX|12345678901|98765432100");
        assertThat(response.accepted()).isTrue();
        assertThat(response.authorizationCode()).startsWith("AUTH-");
        assertThat(response.clearingChannel()).isEqualTo("MAINFRAME_SPI");
    }

    @Test
    void shouldTranslateRefundRequestToLegacyPipeFrame() {
        LegacyMainframeClient spyClient = Mockito.spy(new LegacyMainframeClient());
        LegacyMainframeSettlementAdapter adapter = new LegacyMainframeSettlementAdapter(spyClient);

        SettlementResponse response = adapter.refund("AUTH-ABC123", new BigDecimal("50.00"));

        verify(spyClient).transmitPipeFrame("REFUND|AUTH-ABC123|5000");
        assertThat(response.accepted()).isTrue();
        assertThat(response.authorizationCode()).startsWith("REV-");
        assertThat(response.clearingChannel()).isEqualTo("MAINFRAME_REVERSAL");
    }

    @Test
    void shouldThrowBusinessRuleExceptionWhenMainframeReturnsErrorFrame() {
        LegacyMainframeClient mockedClient = Mockito.mock(LegacyMainframeClient.class);
        when(mockedClient.transmitPipeFrame(anyString()))
                .thenReturn("ERR|CHANNEL_TIMEOUT|Legacy clearing gateway unavailable");
        LegacyMainframeSettlementAdapter adapter = new LegacyMainframeSettlementAdapter(mockedClient);

        assertThatThrownBy(() -> adapter.settle(new SettlementRequest(
                "TXN-20261119-000003",
                new BigDecimal("10.00"),
                PaymentMethod.BOLETO,
                "12345678901",
                "98765432100"
        )))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CHANNEL_TIMEOUT")
                .hasMessageContaining("Legacy clearing gateway unavailable");
    }
}
