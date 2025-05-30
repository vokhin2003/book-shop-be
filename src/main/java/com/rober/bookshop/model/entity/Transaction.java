package com.rober.bookshop.model.entity;

import com.rober.bookshop.enums.PaymentMethod;
import com.rober.bookshop.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction extends Base {

    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId; // Mã giao dịch duy nhất (vnp_TxnRef, mã Momo, hoặc mã tự sinh cho COD)

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // Số tiền giao dịch

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod; // Phương thức thanh toán (COD, VNPAY, MOMO, ZALOPAY, BANK_TRANSFER)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status; // Trạng thái giao dịch (PENDING, SUCCESS, FAILED, REFUNDED, CANCELLED)

    @Column(name = "transaction_date", nullable = false)
    private Instant transactionDate; // Thời gian thực hiện giao dịch

    @Column(name = "response_code", length = 50)
    private String responseCode; // Mã phản hồi từ cổng thanh toán (vnp_ResponseCode, mã lỗi Momo, v.v.)

    @Column(name = "provider_transaction_id", length = 100)
    private String providerTransactionId; // Mã giao dịch từ cổng thanh toán (vnp_TransactionNo, Momo transId, v.v.)

    @Column(name = "bank_code", length = 50)
    private String bankCode; // Mã ngân hàng (vnp_BankCode, hoặc tương tự cho cổng khác)

    @Column(name = "order_info", columnDefinition = "TEXT")
    private String orderInfo; // Thông tin mô tả giao dịch

//    @Column(name = "payment_details", columnDefinition = "TEXT")
//    private String paymentDetails; // Thông tin bổ sung (JSON hoặc text, ví dụ: thông tin thẻ, loại giao dịch)

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // Liên kết với đơn hàng
}