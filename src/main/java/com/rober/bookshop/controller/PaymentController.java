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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final IPaymentService paymentService;
    private final IEmailService emailService;
    private final IOrderService orderService;
    private final IUserService userService;

    @PostMapping("/payments")
//    @Operation(summary = "Create payment URL", description = "Generate a payment URL for the given payment request.")
    public ResponseEntity<PaymentResponseDTO> createPaymentUrl(
            @Valid @RequestBody PaymentRequestDTO dto,
            HttpServletRequest request) {
        return ResponseEntity.ok(this.paymentService.handleCreatePaymentUrl(dto, request));
    }

//    @GetMapping("/payments/vnpay-payment-return")
////    @Operation(summary = "Handle payment result", description = "Process the payment result returned by VNPay.")
//    public ResponseEntity<PaymentResultDTO> getPaymentResult(@RequestParam Map<String, String> params) {
//        return ResponseEntity.ok(this.paymentService.handlePaymentResult(params));
//    }

    @GetMapping("/payments/vnpay-payment-return")
    @ApiMessage("Handle VNPay payment result")
    public ResponseEntity<Void> getPaymentResult(@RequestParam Map<String, String> params) {
        log.info(">>> check params: {}", params);
        PaymentResultDTO result = paymentService.handlePaymentResult(params);
        String redirectUrl = null;
        if(result.getStatus() == TransactionStatus.SUCCESS){
           redirectUrl = "http://localhost:3000/payment/return?transactionId=" + result.getTransactionId();

           String orderId = result.getTransactionId().split("_")[0];

            OrderResponseDTO respDTO = orderService.fetchOrderById(Long.parseLong(orderId));

            User userDB = this.userService.getUserById(respDTO.getUserId());

            this.emailService.sendOrderSuccessEmail(userDB, respDTO);
        }
        else{
            redirectUrl = "http://localhost:3000/payment/return?error=" + URLEncoder.encode(result.getMessage(), StandardCharsets.UTF_8);
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl)
                .build();
    }

    @GetMapping("/transactions/{transactionId}")
    @ApiMessage("Get transaction status")
    public ResponseEntity<PaymentResultDTO> getTransactionStatus(@PathVariable String transactionId) {
        return ResponseEntity.ok(paymentService.getTransactionStatus(transactionId));
    }

}
