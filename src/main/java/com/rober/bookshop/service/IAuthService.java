package com.rober.bookshop.service;

import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.*;
import com.rober.bookshop.model.response.LoginResponseDTO;
import com.rober.bookshop.model.response.RegisterResponseDTO;

public interface IAuthService {

    RegisterResponseDTO register(RegisterRequestDTO requestDTO, String clientPlatform);
    void resendVerification(String email, String clientPlatform);
    void verifyUser(String token);

    LoginResponseDTO handleUserLogin(LoginRequestDTO reqLoginDTO);
    void handleUserLogout();

    void saveRefreshToken(User user, String refreshToken);
    void saveRefreshTokenByEmail(String email, String refreshToken);
    LoginResponseDTO fetchUserByRefreshToken(String refreshToken);
    LoginResponseDTO.UserGetAccount fetchAccount();

    void handleChangePassword(ChangePasswordRequestDTO reqDTO);
    void createPassword(CreatePasswordRequestDTO request);

    LoginResponseDTO outboundAuthenticate(String code);

    void handleForgotPassword(ForgotPasswordRequestDTO request, String clientPlatform);
    String handleResetRedirect(String token, String clientPlatform);
    void handleResetPassword(ResetPasswordRequestDTO request);
}


