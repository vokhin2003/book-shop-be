package com.rober.bookshop.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CartResponseDTO {

    private Long id;
    private Integer quantity;
    private Instant createdAt;
    private Instant updatedAt;
    private BookResponseDTO book;

}
