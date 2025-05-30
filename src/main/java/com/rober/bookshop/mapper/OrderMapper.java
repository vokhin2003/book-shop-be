package com.rober.bookshop.mapper;

import com.rober.bookshop.model.entity.Order;
import com.rober.bookshop.model.request.CreateOrderRequestDTO;
import com.rober.bookshop.model.response.OrderResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    Order toOrder(CreateOrderRequestDTO reqDTO);

    @Mapping(target = "userId", source = "user.id")
    OrderResponseDTO toResponseDTO(Order order);

}
