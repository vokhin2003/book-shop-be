package com.rober.bookshop.model.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class BookResponseDTO {

    private Long id;
    private String thumbnail;
    private List<String> slider;
    private String title;
    private String author;
    private BigDecimal price;
    private Integer quantity;
    private CategoryResponseDTO category;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    private Integer discount;
    private Integer sold;
    private Integer age;
    private Instant publicationDate;
    private String publisher;
    private Integer pageCount;
    private String coverType;

}
