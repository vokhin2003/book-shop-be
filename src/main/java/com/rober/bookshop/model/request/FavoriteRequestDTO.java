package com.rober.bookshop.model.request;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteRequestDTO {

    @NotNull(message = "Book Id must be required")
    private Long bookId;
}
