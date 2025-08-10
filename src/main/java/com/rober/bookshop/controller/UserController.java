package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.CreatePasswordRequestDTO;
import com.rober.bookshop.model.request.DeviceTokenRequestDTO;
import com.rober.bookshop.model.request.UserInfoRequestDTO;
import com.rober.bookshop.model.request.UserRequestDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.model.response.UserResponseDTO;
import com.rober.bookshop.service.IUserDeviceTokenService;
import com.rober.bookshop.service.IUserService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {

    private final IUserService userService;
    private final IUserDeviceTokenService userDeviceTokenService;

    @PostMapping("/users")
    @ApiMessage("Create a user")
    @Operation(summary = "Create a user", description = "Create a new user and return the created user details.")
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO reqDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.create(reqDTO));
    }

    @PutMapping("/users/{id}")
    @ApiMessage("Update a user")
    @Operation(summary = "Update a user", description = "Update an existing user by id and return the updated user details.")
    public ResponseEntity<UserResponseDTO> update(@PathVariable("id") Long id, @Valid @RequestBody UserRequestDTO reqDTO) {
        return ResponseEntity.ok(this.userService.update(id, reqDTO));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete a user by id")
    @Operation(summary = "Delete a user", description = "Delete a user by id.")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        this.userService.delete(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/users")
    @ApiMessage("Fetch all users")
    @Operation(summary = "Get users with pagination", description = "Get a paginated list of users with optional filtering.")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(@Filter Specification<User> spec, Pageable pageable) {
        return ResponseEntity.ok(this.userService.getAllUsers(spec, pageable));
    }

    @PutMapping("/users/info")
    @ApiMessage("User update info")
    @Operation(summary = "User update info", description = "Update the information of the currently logged-in user.")
    public ResponseEntity<UserResponseDTO> userUpdateInfo(@Valid @RequestBody UserInfoRequestDTO reqDTO) {
        return ResponseEntity.ok(this.userService.handleUpdateInfo(reqDTO));
    }

    @PostMapping("/users/device-token")
    @ApiMessage("Save user device token")
    public ResponseEntity<Void> saveDeviceToken(@RequestBody @Valid DeviceTokenRequestDTO request) {
        this.userDeviceTokenService.saveDeviceToken(request);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/users/remove-device-token")
    @ApiMessage("Remove user device token")
    public ResponseEntity<Void> removeDeviceToken(@RequestBody @Valid DeviceTokenRequestDTO request) {
        this.userDeviceTokenService.removeDeviceToken(request);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/users/create-password")
    public ResponseEntity<Void> createPassword(@RequestBody @Valid CreatePasswordRequestDTO request) {
        this.userService.createPassword(request);
        return ResponseEntity.ok(null);
    }

    @PutMapping("/admin/users/{id}/status")
    @ApiMessage("Toggle user admin active status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle isAdminActive", description = "Enable or disable a user's admin active status")
    public ResponseEntity<UserResponseDTO> toggleAdminActive(
            @PathVariable("id") Long id,
            @RequestParam("isAdminActive") boolean isAdminActive
    ) {
        return ResponseEntity.ok(this.userService.toggleAdminActive(id, isAdminActive));
    }
}
