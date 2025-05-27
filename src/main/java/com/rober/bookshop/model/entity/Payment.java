//package com.rober.bookshop.model.entity;
//
//import jakarta.persistence.*;
//
//import java.math.BigDecimal;
//
//@Entity
//@Table(name = "payments")
//public class Payment extends Base {
//
//    @Column(name = "transaction_id", unique = true, length = 100)
//    private String transactionId;
//
//    @Column(nullable = false, precision = 10, scale = 2)
//    private BigDecimal amount;
//
//    @Column(name = "payment_status", nullable = false, length = 50)
//    private String paymentStatus;
//
//    @Column(name = "payment_method", nullable = false, length = 50)
//    private String paymentMethod;
//
//    @Column(name = "response_data", columnDefinition = "JSONB")
//    private String responseData;
//
////    @ManyToOne
////    @JoinColumn(name = "order_id", nullable = false)
////    private Order order;
//
//}
