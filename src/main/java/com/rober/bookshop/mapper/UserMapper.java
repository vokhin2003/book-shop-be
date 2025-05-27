package com.rober.bookshop.mapper;

import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.UserRequestDTO;
import com.rober.bookshop.model.response.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", ignore = true)
    User toUser(UserRequestDTO dto);

//    @Mapping(source = "role.name", target = "role")
    UserResponseDTO toResponseDTO(User user);

    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateUserFromDTO(UserRequestDTO dto, @MappingTarget User user);

}
