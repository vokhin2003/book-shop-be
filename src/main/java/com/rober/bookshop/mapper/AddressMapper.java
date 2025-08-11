package com.rober.bookshop.mapper;

import com.rober.bookshop.model.entity.Address;
import com.rober.bookshop.model.request.AddressRequestDTO;
import com.rober.bookshop.model.response.AddressResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    @Mapping(target = "user", ignore = true)
    Address toEntity(AddressRequestDTO dto);

    AddressResponseDTO toResponse(Address entity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateFromDTO(AddressRequestDTO dto, @MappingTarget Address entity);
}


