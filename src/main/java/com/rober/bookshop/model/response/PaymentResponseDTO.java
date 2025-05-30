package com.rober.bookshop.model.response;

import com.rober.bookshop.enums.PaymentMethod;
import com.rober.bookshop.enums.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponseDTO {

    private String paymentUrl; // URL thanh toán (null cho COD hoặc chuyển khoản)

    private String transactionId; // Mã giao dịch

    private PaymentMethod paymentMethod; // Phương thức thanh toán

    private TransactionStatus status; // Trạng thái giao dịch

    private String message; // Thông tin mô tả (ví dụ: thông tin tài khoản ngân hàng cho chuyển khoản)
}