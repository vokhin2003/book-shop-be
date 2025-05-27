package com.rober.bookshop.service;

import com.rober.bookshop.model.entity.Role;
import com.rober.bookshop.model.request.RoleRequestDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.model.response.RoleResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface IRoleService {
    Role getCustomerRole();
    RoleResponseDTO create(RoleRequestDTO reqRoleDTO);
    RoleResponseDTO update(Long id, RoleRequestDTO reqDTO);
    void delete(Long id);

    ResultPaginationDTO getAll(Specification<Role> spec, Pageable pageable);

}
