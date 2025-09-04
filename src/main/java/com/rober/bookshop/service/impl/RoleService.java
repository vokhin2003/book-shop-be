package com.rober.bookshop.service.impl;

import com.rober.bookshop.exception.BadRequestException;
import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.mapper.RoleMapper;
import com.rober.bookshop.model.entity.Permission;
import com.rober.bookshop.model.entity.Role;
import com.rober.bookshop.model.request.RoleRequestDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.model.response.RoleResponseDTO;
import com.rober.bookshop.repository.PermissionRepository;
import com.rober.bookshop.repository.RoleRepository;
import com.rober.bookshop.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;

    @Override
    public Role getCustomerRole() {
        return this.roleRepository.findByName("CUSTOMER").orElseThrow(() -> new IdInvalidException("Role CUSTOMER not found"));
    }

    @Override
    public RoleResponseDTO create(RoleRequestDTO reqRoleDTO) {
        if (this.roleRepository.existsByName(reqRoleDTO.getName())) {
            throw new IdInvalidException("Role name already exists.");
        }

        Role role = this.roleMapper.toRole(reqRoleDTO);

        if (reqRoleDTO.getPermissionIds() != null) {
            List<Permission> permissions = this.permissionRepository.findByIdIn(reqRoleDTO.getPermissionIds());
            role.setPermissions(permissions);
        }

        Role newRole = this.roleRepository.save(role);

        return roleMapper.toResDTO(newRole);
    }

    @Override
    public RoleResponseDTO update(Long id, RoleRequestDTO reqDTO) {
        Role existingRole = this.roleRepository.findById(id).orElseThrow(() -> new IdInvalidException("Role with id = " + id + " not found"));

        this.roleMapper.updateRoleFromDTO(reqDTO, existingRole);
        if (reqDTO.getPermissionIds() != null) {
            List<Permission> permissions = this.permissionRepository.findByIdIn(reqDTO.getPermissionIds());
            existingRole.setPermissions(permissions);
        }

        return this.roleMapper.toResDTO(this.roleRepository.save(existingRole));
    }

    @Override
    public void delete(Long id) {
        Role deletedRole = this.roleRepository.findById(id).orElseThrow(() -> new IdInvalidException("Role with id = " + id + " not found"));

        if (!deletedRole.getUsers().isEmpty()) {
            throw new BadRequestException("Cannot delete role with id = " + id + " because an existing user has this role");
        }

        this.roleRepository.delete(deletedRole);
    }

    @Override
    public ResultPaginationDTO getAll(Specification<Role> spec, Pageable pageable) {
        Page<Role> rolePage = this.roleRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setCurrent(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(rolePage.getTotalPages());
        meta.setTotal(rolePage.getTotalElements());

        res.setMeta(meta);

        List<RoleResponseDTO> listRole = rolePage.getContent().stream().map(this.roleMapper::toResDTO).toList();

        res.setResult(listRole);
        return res;
    }
}
