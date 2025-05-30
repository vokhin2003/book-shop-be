package com.rober.bookshop.model.request;

import com.rober.bookshop.enums.PaymentMethod;
import com.rober.bookshop.validation.AdminCreateOrderValidationGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequestDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotNull(message = "Items is required")
    @NotEmpty(message = "Items cannot be empty")
    @Valid
    private List<OrderItemRequestDTO> items;
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "User ID is required for admin", groups = AdminCreateOrderValidationGroup.class)
    @Positive(message = "User ID must be positive", groups = AdminCreateOrderValidationGroup.class)
    private Long userId;

}
