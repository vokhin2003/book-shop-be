package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.UserInfoRequestDTO;
import com.rober.bookshop.model.request.UserRequestDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.model.response.UserResponseDTO;
import com.rober.bookshop.service.IUserService;
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
public class UserController {

    private final IUserService userService;

    @PostMapping("/users")
    @ApiMessage("Create a user")
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO reqDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.create(reqDTO));
    }

    @PutMapping("/users/{id}")
    @ApiMessage("Update a user")
    public ResponseEntity<UserResponseDTO> update(@PathVariable("id") Long id, @Valid @RequestBody UserRequestDTO reqDTO) {
        return ResponseEntity.ok(this.userService.update(id, reqDTO));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete a user by id")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        this.userService.delete(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/users")
    @ApiMessage("Fetch all users")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(@Filter Specification<User> spec, Pageable pageable) {
        return ResponseEntity.ok(this.userService.getAllUsers(spec, pageable));
    }

    @PutMapping("/users/info")
    @ApiMessage("User update info")
    public ResponseEntity<UserResponseDTO> userUpdateInfo(@Valid @RequestBody UserInfoRequestDTO reqDTO) {
        return ResponseEntity.ok(this.userService.handleUpdateInfo(reqDTO));
    }

}
