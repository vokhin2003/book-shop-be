package com.rober.bookshop.mapper;

import com.rober.bookshop.model.entity.Cart;
import com.rober.bookshop.model.response.CartResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper {

    CartResponseDTO toResponseDTO(Cart cart);

}
