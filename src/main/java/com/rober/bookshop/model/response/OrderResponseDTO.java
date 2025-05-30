package com.rober.bookshop.model.response;

import com.rober.bookshop.enums.OrderStatus;
import com.rober.bookshop.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class OrderResponseDTO {

    private Long id;
    private String fullName;
    private String phone;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String shippingAddress;
    private PaymentMethod paymentMethod;
    private Long userId;
    private List<OrderItemResponseDTO> orderItems;
    private Instant createdAt;
    private Instant updatedAt;

}
