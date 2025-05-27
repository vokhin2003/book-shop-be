package com.rober.bookshop.service;

import com.rober.bookshop.model.entity.Permission;
import com.rober.bookshop.model.request.PermissionRequestDTO;
import com.rober.bookshop.model.response.PermissionResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface IPermissionService {

    PermissionResponseDTO create(PermissionRequestDTO reqDTO);
    PermissionResponseDTO update(Long id, PermissionRequestDTO reqDTO);
    void delete(Long id);

    ResultPaginationDTO getAll(Specification<Permission> spec, Pageable pageable);

}
