package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.model.entity.Permission;
import com.rober.bookshop.model.request.PermissionRequestDTO;
import com.rober.bookshop.model.response.PermissionResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.service.IPermissionService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PermissionController {

    private final IPermissionService permissionService;

    @PostMapping("/permissions")
    @ApiMessage("Create a permission")
    public ResponseEntity<PermissionResponseDTO> create(@Valid @RequestBody PermissionRequestDTO reqDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.permissionService.create(reqDTO));
    }

    @PutMapping("/permissions/{id}")
    @ApiMessage("Update a permission")
    public ResponseEntity<PermissionResponseDTO> update(@PathVariable Long id, @Valid @RequestBody PermissionRequestDTO reqDTO) {
        return ResponseEntity.ok(this.permissionService.update(id, reqDTO));
    }

    @DeleteMapping("/permissions/{id}")
    @ApiMessage("Delete a permission by id")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        this.permissionService.delete(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/permissions")
    @ApiMessage("Get permission with pagination")
    public ResponseEntity<ResultPaginationDTO> getAll(@Filter Specification<Permission> spec, Pageable pageable) {
        return ResponseEntity.ok(this.permissionService.getAll(spec, pageable));
    }

}
