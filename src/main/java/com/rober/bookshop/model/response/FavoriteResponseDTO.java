package com.rober.bookshop.model.response;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteResponseDTO {
    private Long id;
    private Long userId;
    private Long bookId;
}
