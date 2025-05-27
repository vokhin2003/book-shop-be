package com.rober.bookshop.mapper;

import com.rober.bookshop.model.entity.Role;
import com.rober.bookshop.model.request.RoleRequestDTO;
import com.rober.bookshop.model.response.RoleResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoleMapper {


    RoleResponseDTO toResDTO(Role role);
    Role toRole(RoleRequestDTO reqRoleDTO);
    void updateRoleFromDTO(RoleRequestDTO reqRoleDTO, @MappingTarget Role role);

}
