package com.rober.bookshop.model.request;

import com.rober.bookshop.validation.BookUpdateValidationGroup;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class BookRequestDTO {

    @NotBlank(message = "Thumbnail is required")
    private String thumbnail;
    @NotNull(message = "Slider is required")
    private List<String> slider;
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Author is required")
    private String author;
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    private Long category;

    private String description;

//    @NotNull(message = "Discount is required", groups = {BookUpdateValidationGroup.class})
//    @Min(value = 0, message = "Discount must be at least 0", groups = {BookUpdateValidationGroup.class})
//    @Max(value = 100, message = "Discount must not exceed 100", groups = {BookUpdateValidationGroup.class})
//    private Integer discount;
//
//    @NotNull(message = "Sold is required", groups = {BookUpdateValidationGroup.class})
//    @Min(value = 0, message = "Sold must be at least 0", groups = {BookUpdateValidationGroup.class})
//    private Integer sold;

    @Min(value = 0, message = "Discount must be at least 0")
    @Max(value = 100, message = "Discount must not exceed 100")
    @NotNull(message = "Discount is required", groups = {BookUpdateValidationGroup.class})
    private Integer discount;

    @Min(value = 0, message = "Sold must be at least 0")
    @NotNull(message = "Sold is required", groups = {BookUpdateValidationGroup.class})
    private Integer sold;

    @NotNull(message = "Age is required")
    @Positive(message = "Age must be positive")
    private Integer age;
    @NotNull(message = "Publication date is required")
    private Instant publicationDate;
    @NotBlank(message = "Publisher is required")
    private String publisher;
    @NotNull(message = "Page count is required")
    @Positive(message = "Page count must be positive")
    private Integer pageCount;
    @NotBlank(message = "Cover type is required")
    private String coverType;

}
