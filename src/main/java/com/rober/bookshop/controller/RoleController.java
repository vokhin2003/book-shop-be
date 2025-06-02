package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.model.entity.Role;
import com.rober.bookshop.model.request.RoleRequestDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.model.response.RoleResponseDTO;
import com.rober.bookshop.service.impl.RoleService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Role")
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/roles")
    @ApiMessage("Create a role")
    @Operation(summary = "Create a role", description = "Create a new role and return the created role details.")
    public ResponseEntity<RoleResponseDTO> create(@Valid @RequestBody RoleRequestDTO reqDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.roleService.create(reqDTO));
    }

    @PutMapping("/roles/{id}")
    @ApiMessage("Update a role")
    @Operation(summary = "Update a role", description = "Update an existing role by id and return the updated role details.")
    public ResponseEntity<RoleResponseDTO> update(@PathVariable("id") Long id, @Valid @RequestBody RoleRequestDTO reqDTO) {
        return ResponseEntity.ok(this.roleService.update(id, reqDTO));
    }

    @DeleteMapping("/roles/{id}")
    @ApiMessage("Delete role by id")
    @Operation(summary = "Delete a role", description = "Delete a role by id.")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        this.roleService.delete(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/roles")
    @ApiMessage("Fetch all roles")
    @Operation(summary = "Get roles with pagination", description = "Get a paginated list of roles with optional filtering.")
    public ResponseEntity<ResultPaginationDTO> getAll(@Filter Specification<Role> spec, Pageable pageable) {
        return ResponseEntity.ok(this.roleService.getAll(spec, pageable));
    }

}
