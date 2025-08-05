package com.rober.bookshop.mapper;


import com.rober.bookshop.model.entity.Favorite;
import com.rober.bookshop.model.response.FavoriteResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FavoriteMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "bookId", source = "book.id")
    FavoriteResponseDTO toResponse(Favorite favorite);
}
