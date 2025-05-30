package com.rober.bookshop.service;

import com.rober.bookshop.model.request.PaymentRequestDTO;
import com.rober.bookshop.model.response.PaymentResponseDTO;
import com.rober.bookshop.model.response.PaymentResultDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Map;

public interface IPaymentService {

    PaymentResponseDTO handleCreatePaymentUrl(@Valid PaymentRequestDTO dto, HttpServletRequest request);

    PaymentResultDTO handlePaymentResult(Map<String, String> params);
    PaymentResultDTO getTransactionStatus(String transactionId);

}
