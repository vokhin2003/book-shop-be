package com.rober.bookshop.model.request;

import com.rober.bookshop.enums.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderRequestDTO {
    @NotBlank(message = "Full name is required")
    private String fullName;
    @NotBlank(message = "Phone number is required")
    private String phone;
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
    @NotNull(message = "Order method is required")
    private OrderStatus status;

}
