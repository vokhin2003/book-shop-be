package com.rober.bookshop.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequestDTO {

    @NotNull(message = "Book id is required")
    @Min(value = 1, message = "Book ID must be positive")
    private Long bookId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

}
