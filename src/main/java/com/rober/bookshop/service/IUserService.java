package com.rober.bookshop.service;

import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.LoginRequestDTO;
import com.rober.bookshop.model.request.RegisterRequestDTO;
import com.rober.bookshop.model.request.UserRequestDTO;
import com.rober.bookshop.model.response.LoginResponseDTO;
import com.rober.bookshop.model.response.RegisterResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.model.response.UserResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface IUserService {

    User handleGetUserByUsername(String username);
    RegisterResponseDTO register(RegisterRequestDTO requestDTO);
    void verifyUser(String token);
    LoginResponseDTO handleUserLogin(LoginRequestDTO reqLoginDTO);
    void saveRefreshToken(User user, String refreshToken);
    LoginResponseDTO.UserGetAccount fetchAccount();
    LoginResponseDTO fetchUserByRefreshToken(String refreshToken);


    UserResponseDTO create(UserRequestDTO reqDTO);
    UserResponseDTO update(Long id, UserRequestDTO reqDTO);
    void delete(Long id);

    ResultPaginationDTO getAllUsers(Specification<User> spec, Pageable pageable);
    void handleUserLogout();

}
