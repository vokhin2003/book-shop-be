package com.rober.bookshop.service.impl;

import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.mapper.PermissionMapper;
import com.rober.bookshop.model.entity.Permission;
import com.rober.bookshop.model.request.PermissionRequestDTO;
import com.rober.bookshop.model.response.PermissionResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.repository.PermissionRepository;
import com.rober.bookshop.service.IPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService implements IPermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public boolean isPermissionExist(PermissionRequestDTO reqDTO) {
        return this.permissionRepository.existsByPathAndMethodAndModule(reqDTO.getPath(), reqDTO.getMethod(), reqDTO.getModule());
    }


    @Override
    public PermissionResponseDTO create(PermissionRequestDTO reqDTO) {
        if (isPermissionExist(reqDTO)) {
            throw new IdInvalidException("Permission already exists.");
        }

        if (this.permissionRepository.existsByName(reqDTO.getName())) {
            throw new IdInvalidException("Permission name is unique");
        }

        Permission permission = this.permissionMapper.toPermission(reqDTO);

        return this.permissionMapper.toResponseDTO(this.permissionRepository.save(permission));
    }

    @Override
    public PermissionResponseDTO update(Long id, PermissionRequestDTO reqDTO) {
        Permission existingPermission = this.permissionRepository.findById(id).orElseThrow(() -> new IdInvalidException("Permission with id = " + id + " not found"));

        if(!existingPermission.getName().equals(reqDTO.getName())) {
            if (this.permissionRepository.existsByName(reqDTO.getName())) {
                throw new IdInvalidException("Permission name is already exists.");
            }
        }

        if (!existingPermission.getPath().equals(reqDTO.getPath()) || !existingPermission.getModule().equals(reqDTO.getModule()) || !existingPermission.getMethod().equals(reqDTO.getMethod())) {
            if (isPermissionExist(reqDTO)) {
                throw new IdInvalidException("Permission already exists.");
            }
        }

        this.permissionMapper.updatePermissionFromDTO(reqDTO, existingPermission);

        return this.permissionMapper.toResponseDTO(this.permissionRepository.save(existingPermission));
    }

    @Override
    public void delete(Long id) {
        Permission deletedPermission = this.permissionRepository.findById(id).orElseThrow(() -> new IdInvalidException("Permission with id = " + id + " not found"));
        deletedPermission.getRoles().forEach(role -> role.getPermissions().remove(deletedPermission));
        this.permissionRepository.delete(deletedPermission);
    }

    @Override
    public ResultPaginationDTO getAll(Specification<Permission> spec, Pageable pageable) {
        Page<Permission> permissionPage = this.permissionRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setCurrent(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(permissionPage.getTotalPages());
        meta.setTotal(permissionPage.getTotalElements());

        res.setMeta(meta);

        List<PermissionResponseDTO> listPermissions = permissionPage.getContent().stream().map(this.permissionMapper::toResponseDTO).toList();

        res.setResult(listPermissions);
        return res;
    }
}
