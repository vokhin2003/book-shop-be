package com.rober.bookshop.model.response;

import com.rober.bookshop.enums.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResultDTO {
    private String transactionId;
    private TransactionStatus status;
    private String message;
}