package com.rober.bookshop.enums;

public enum TransactionStatus {

    PENDING, // Giao dịch đang chờ xử lý
    SUCCESS, // Giao dịch thành công
    FAILED,  // Giao dịch thất bại
    REFUNDED, // Giao dịch đã hoàn tiền
    CANCELLED // Giao dịch bị hủy

}
