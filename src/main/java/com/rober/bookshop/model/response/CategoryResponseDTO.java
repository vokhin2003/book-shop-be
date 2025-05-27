package com.rober.bookshop.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CategoryResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

}
