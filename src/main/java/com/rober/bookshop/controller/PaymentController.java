package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.enums.TransactionStatus;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.PaymentRequestDTO;
import com.rober.bookshop.model.response.OrderResponseDTO;
import com.rober.bookshop.model.response.PaymentResponseDTO;
import com.rober.bookshop.model.response.PaymentResultDTO;
import com.rober.bookshop.service.IEmailService;
import com.rober.bookshop.service.IOrderService;
import com.rober.bookshop.service.IPaymentService;
import com.rober.bookshop.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment")
public class PaymentController {

    private final IPaymentService paymentService;
    private final IEmailService emailService;
    private final IOrderService orderService;
    private final IUserService userService;

    @PostMapping("/payments")
    @Operation(summary = "Create payment URL", description = "Generate a payment URL for the given payment request.")
    public ResponseEntity<PaymentResponseDTO> createPaymentUrl(
            @Valid @RequestBody PaymentRequestDTO dto,
            @RequestHeader(value = "X-Client-Platform", defaultValue = "web") String deviceType,
            HttpServletRequest request) {
        return ResponseEntity.ok(this.paymentService.handleCreatePaymentUrl(dto, request, deviceType));
    }

//    @GetMapping("/payments/vnpay-payment-return")
////    @Operation(summary = "Handle payment result", description = "Process the payment result returned by VNPay.")
//    public ResponseEntity<PaymentResultDTO> getPaymentResult(@RequestParam Map<String, String> params) {
//        return ResponseEntity.ok(this.paymentService.handlePaymentResult(params));
//    }

    @GetMapping("/payments/vnpay-payment-return")
    @ApiMessage("Handle VNPay payment result")
    @Operation(summary = "Handle VNPay payment result", description = "Process the payment result returned by VNPay and redirect to the frontend.")
    public ResponseEntity<Void> getPaymentResult(@RequestParam Map<String, String> params) {
        log.info(">>> check params: {}", params);
        PaymentResultDTO result = paymentService.handlePaymentResult(params);

//        String redirectUrl = result.getStatus() == TransactionStatus.SUCCESS
//                ? "http://localhost:3000/payment/return/" + result.getTransactionId()
//                : "http://localhost:3000/payment/return?error=" + URLEncoder.encode(result.getMessage(), StandardCharsets.UTF_8);


        String deviceType = result.getTransactionId().split("_")[1];

        String redirectUrl =deviceType.equals("web") ?
                "http://localhost:3000/payment/return/" + result.getTransactionId()
                :
                "http://bromel.free.nf/payment-result.html?transactionId=" + result.getTransactionId()
                ;

        if (result.getStatus() == TransactionStatus.SUCCESS) {

            String orderId = result.getTransactionId().split("_")[0];

            OrderResponseDTO respDTO = orderService.fetchOrderById(Long.parseLong(orderId));

            User userDB = this.userService.getUserById(respDTO.getUserId());

            this.emailService.sendOrderSuccessEmail(userDB, respDTO);
        }


        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl)
                .build();
    }

    @GetMapping("/transactions/{transactionId}")
    @ApiMessage("Get transaction status")
    @Operation(summary = "Get transaction status", description = "Get the status of a transaction by its transaction id.")
    public ResponseEntity<PaymentResultDTO> getTransactionStatus(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getTransactionStatus(transactionId));
    }

}
