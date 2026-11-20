package io.github.davigdomingues.paymenthub.dto.request;

import io.github.davigdomingues.paymenthub.domain.enums.CustomerTier;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Payload imutável de entrada para processamento e simulação de pagamentos.
 *
 * <p><strong>Fronteira de validação e impacto:</strong> aplica Bean Validation na borda
 * HTTP ({@code @Valid}), convertendo erros sintáticos (formato de CPF/CNPJ, valor mínimo,
 * faixa de parcelas) em HTTP {@code 400 Bad Request} via {@code GlobalExceptionHandler} antes
 * de alcançar as regras semânticas de {@code PaymentStrategy} e compliance (HTTP {@code 422}).</p>
 */
public record CreatePaymentRequest(
        @NotBlank(message = "payerDocument must not be blank")
        @Pattern(regexp = "\\d{11}|\\d{14}", message = "payerDocument must be an 11-digit CPF or 14-digit CNPJ")
        String payerDocument,

        @NotBlank(message = "payeeDocument must not be blank")
        @Pattern(regexp = "\\d{11}|\\d{14}", message = "payeeDocument must be an 11-digit CPF or 14-digit CNPJ")
        String payeeDocument,

        @NotNull(message = "amount must not be null")
        @DecimalMin(value = "0.01", message = "amount must be at least 0.01")
        BigDecimal amount,

        @NotNull(message = "paymentMethod must not be null")
        PaymentMethod paymentMethod,

        CustomerTier customerTier,

        @Min(value = 1, message = "installments must be at least 1")
        @Max(value = 12, message = "installments must not exceed 12")
        Integer installments,

        String pixKey,

        @Size(max = 100, message = "idempotencyKey must not exceed 100 characters")
        String idempotencyKey,

        @Size(max = 255, message = "description must not exceed 255 characters")
        String description
) {
    /**
     * Retorna o nível de relacionamento informado ou {@link CustomerTier#STANDARD} por padrão.
     *
     * @return nível de relacionamento efetivo
     */
    public CustomerTier resolvedCustomerTier() {
        return customerTier != null ? customerTier : CustomerTier.STANDARD;
    }

    /**
     * Retorna o número de parcelas informado ou {@code 1} por padrão.
     *
     * @return quantidade efetiva de parcelas
     */
    public int resolvedInstallments() {
        return installments != null ? installments : 1;
    }
}
