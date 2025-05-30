package com.rober.bookshop.controller;

import com.rober.bookshop.model.request.PaymentRequestDTO;
import com.rober.bookshop.model.response.PaymentResponseDTO;
import com.rober.bookshop.service.IPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping("/payments")
//    @Operation(summary = "Create payment URL", description = "Generate a payment URL for the given payment request.")
    public ResponseEntity<PaymentResponseDTO> createPaymentUrl(
            @Valid @RequestBody PaymentRequestDTO dto,
            HttpServletRequest request) {
        return ResponseEntity.ok(this.paymentService.handleCreatePaymentUrl(dto, request));
    }

    @GetMapping("/payments/vnpay-payment-return")
//    @Operation(summary = "Handle payment result", description = "Process the payment result returned by VNPay.")
    public ResponseEntity<Void> getPaymentResult(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(this.paymentService.handlePaymentResult(params));
    }

}
