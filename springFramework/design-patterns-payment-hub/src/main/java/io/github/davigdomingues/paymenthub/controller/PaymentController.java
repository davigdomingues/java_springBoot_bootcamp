package io.github.davigdomingues.paymenthub.controller;

import io.github.davigdomingues.paymenthub.domain.enums.PaymentMethod;
import io.github.davigdomingues.paymenthub.domain.enums.PaymentStatus;
import io.github.davigdomingues.paymenthub.dto.request.CreatePaymentRequest;
import io.github.davigdomingues.paymenthub.dto.response.ApiErrorResponse;
import io.github.davigdomingues.paymenthub.dto.response.AuditLogResponse;
import io.github.davigdomingues.paymenthub.dto.response.NotificationResponse;
import io.github.davigdomingues.paymenthub.dto.response.PaymentResponse;
import io.github.davigdomingues.paymenthub.dto.response.PaymentSimulationResponse;
import io.github.davigdomingues.paymenthub.pattern.facade.PaymentOrchestrationFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * Controlador REST fino para processamento, simulação, consulta, estorno, auditoria e notificações
 * de pagamentos.
 *
 * Estrutura e impacto: atua exclusivamente como adaptador HTTP na fronteira da aplicação, aplicando
 * Bean Validation ({@code @Valid}) nos payloads de entrada e delegando toda regra de negócio ao
 * {@link PaymentOrchestrationFacade}. Erros sintáticos são convertidos em HTTP {@code 400},
 * ausência de recursos em {@code 404} e violações de negócio/compliance em {@code 422} pelo
 * {@code GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Orquestração e consulta de pagamentos via Facade")
public class PaymentController {

    private final PaymentOrchestrationFacade paymentFacade;

    /**
     * @throws NullPointerException quando {@code paymentFacade} for nulo
     */
    public PaymentController(PaymentOrchestrationFacade paymentFacade) {
        this.paymentFacade = Objects.requireNonNull(paymentFacade, "paymentFacade must not be null");
    }

    @PostMapping
    @Operation(
            summary = "Process a payment transaction",
            description = "Executa validação estratégica, análise de compliance, liquidação legada, cashback, telemetria e observadores."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment processed or retrieved by idempotency key"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation or malformed payload error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Business rule or compliance violation",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentFacade.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/simulate")
    @Operation(
            summary = "Simulate payment fees, cashback and risk assessment",
            description = "Executa cálculos de Strategy, cashback e Chain of Responsibility sem persistir nem liquidar."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Simulation calculated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Business rule violation",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<PaymentSimulationResponse> simulatePayment(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(paymentFacade.simulatePayment(request));
    }

    @GetMapping
    @Operation(summary = "List payment transactions with optional filters")
    public ResponseEntity<List<PaymentResponse>> listPayments(
            @RequestParam(required = false) PaymentMethod method,
            @RequestParam(required = false) PaymentStatus status
    ) {
        return ResponseEntity.ok(paymentFacade.listPayments(method, status));
    }

    @GetMapping("/{transactionCode}")
    @Operation(summary = "Get a payment transaction by its unique code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String transactionCode) {
        return ResponseEntity.ok(paymentFacade.getByTransactionCode(transactionCode));
    }

    @PostMapping("/{transactionCode}/refund")
    @Operation(summary = "Refund an approved or pending settlement payment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment refunded successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Transaction status is not eligible for refund",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable String transactionCode) {
        return ResponseEntity.ok(paymentFacade.refundPayment(transactionCode));
    }

    @GetMapping("/{transactionCode}/audit")
    @Operation(summary = "Get the audit log trail for a transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audit logs retrieved"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<List<AuditLogResponse>> getAuditTrail(@PathVariable String transactionCode) {
        return ResponseEntity.ok(paymentFacade.getAuditTrail(transactionCode));
    }

    @GetMapping("/{transactionCode}/notifications")
    @Operation(summary = "Get dispatched notifications for a transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification logs retrieved"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<List<NotificationResponse>> getNotifications(@PathVariable String transactionCode) {
        return ResponseEntity.ok(paymentFacade.getNotifications(transactionCode));
    }
}
