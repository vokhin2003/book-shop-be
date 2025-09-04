package com.rober.bookshop.service;

import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.*;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.model.response.UserResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface IUserService {

    User handleGetUserByUsername(String username);
    UserResponseDTO create(UserRequestDTO reqDTO);
    UserResponseDTO update(Long id, UserRequestDTO reqDTO);
    void delete(Long id);
    UserResponseDTO handleUpdateInfo(UserInfoRequestDTO reqDTO);
    ResultPaginationDTO getAllUsers(Specification<User> spec, Pageable pageable);
    User getUserLogin();
    User getUserById(Long id);
    UserResponseDTO toggleAdminActive(Long id, boolean isAdminActive);
}
