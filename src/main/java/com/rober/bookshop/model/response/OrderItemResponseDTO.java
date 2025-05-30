package com.rober.bookshop.model.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemResponseDTO {

    private Long id;
    private Integer quantity;
    private BigDecimal price;
    private BookResponseDTO book;

}
