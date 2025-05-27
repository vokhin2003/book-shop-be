package com.rober.bookshop.mapper;

import com.rober.bookshop.model.entity.Permission;
import com.rober.bookshop.model.request.PermissionRequestDTO;
import com.rober.bookshop.model.response.PermissionResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    Permission toPermission(PermissionRequestDTO dto);
    PermissionResponseDTO toResponseDTO(Permission permission);

    void updatePermissionFromDTO(PermissionRequestDTO dto, @MappingTarget Permission permission);

}
