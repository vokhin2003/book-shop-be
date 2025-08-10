package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.*;
import com.rober.bookshop.model.response.LoginResponseDTO;
import com.rober.bookshop.model.response.RegisterResponseDTO;
import com.rober.bookshop.service.IEmailService;
import com.rober.bookshop.service.IUserService;
import com.rober.bookshop.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
@Tag(name = "Auth")
public class AuthController {

    private final IEmailService emailService;
    private final IUserService userService;
    private final SecurityUtil securityUtil;

    @Value("${rober.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;


    @GetMapping("/")
    public String Hello() {
        this.emailService.send("khinvo2003@gmail.com", "Test", "Test123");
        return "Hello wolrd";
    }

    @PostMapping("/auth/register")
    @ApiMessage("Register user")
    @Operation(summary = "Register user", description = "Register a new user and return the registration details.")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO reqUser,
                                                        @RequestHeader(value = "X-Client-Platform", defaultValue = "web") String clientPlatform) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.register(reqUser, clientPlatform));
    }

//    @GetMapping("/auth/verify")
//    @ApiMessage("Verify account")
//    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
//        this.userService.verifyUser(token);
//        return ResponseEntity.ok("Account verified successfully. You can now log in.");
//
////        String redirectUrl = "http://localhost:3000/verify/return?status=success";
////
////        return ResponseEntity.status(HttpStatus.FOUND)
////                .header("Location", redirectUrl)
////                .build();
//    }

    @GetMapping("/auth/verify")
    @ApiMessage("Verify account")
    @Operation(summary = "Verify account", description = "Verify a user's account using a token and redirect to the frontend.")
    public ResponseEntity<Void> verifyUser(@RequestParam("token") String token,
                                           @RequestParam(value = "clientPlatform", defaultValue = "web") String clientPlatform) {

        boolean isWeb = clientPlatform.equalsIgnoreCase("web");
        try {
            this.userService.verifyUser(token);


//            String redirectUrl = "http://localhost:3000/verify/return?status=success";

            String redirectUrl = isWeb ?
                    "http://localhost:3000/verify/return?status=success"
                    :
                    "http://bromel.free.nf/verify-return.html?status=success";

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();
        } catch (IdInvalidException e) {
            log.error("Xác minh thất bại: {}", e.getMessage());
//            String redirectUrl = "http://localhost:3000/verify/return?status=error&message=";

            String redirectUrl = isWeb ?
                    "http://localhost:3000/verify/return?status=error&message="
                    :
                    "http://bromel.free.nf/verify-return.html?status=error&message=";

            redirectUrl += URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();
        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi xác minh: {}", e.getMessage());
//            String redirectUrl = "http://localhost:3000/verify/return?status=error&message="

            String redirectUrl = isWeb ?
                    "http://localhost:3000/verify/return?status=error&message="
                    :
                    "http://bromel.free.nf/verify-return.html?status=error&message=";

            redirectUrl += URLEncoder.encode("Đã xảy ra lỗi không mong muốn", StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();
        }
    }

    @PostMapping("/auth/login")
    @ApiMessage("User login")
    @Operation(summary = "User login", description = "Authenticate a user and return login details with a refresh token.")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO reqLoginDTO) {
        LoginResponseDTO res = this.userService.handleUserLogin(reqLoginDTO);

        String refreshToken = this.securityUtil.createRefreshToken(reqLoginDTO.getUsername(), res);

        User user = userService.handleGetUserByUsername(reqLoginDTO.getUsername());
        if (user != null) {
            // Lưu refresh token vào bảng tokens
            userService.saveRefreshToken(user, refreshToken);
        }

        ResponseCookie resCookie = ResponseCookie
                .from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookie.toString()).body(res);
    }

    @PostMapping("/auth/resend-verify")
    @ApiMessage("Resend verification email")
    @Operation(summary = "Resend verification email", description = "Resend a new verification token for a pending user.")
    public ResponseEntity<Void> resendVerify(@RequestParam("email") String email,
                                               @RequestHeader(value = "X-Client-Platform", defaultValue = "web") String clientPlatform) {
        this.userService.resendVerification(email, clientPlatform);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/auth/forgot-password")
    @ApiMessage("Request forgot password")
    @Operation(summary = "Request forgot password", description = "Send reset password link to user email if exists")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request,
                                               @RequestHeader(value = "X-Client-Platform", defaultValue = "web") String clientPlatform) {
        this.userService.handleForgotPassword(request, clientPlatform);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/auth/reset")
    @ApiMessage("Validate reset token and redirect to FE")
    public ResponseEntity<Void> resetRedirect(@RequestParam("token") String token) {
        String redirect = this.userService.handleResetRedirect(token);
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, redirect).build();
    }

    @PostMapping("/auth/reset")
    @ApiMessage("Reset password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        this.userService.handleResetPassword(request);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/auth/account")
    @ApiMessage("Get user information")
    @Operation(summary = "Get user information", description = "Get information of the currently logged-in user.")
    public ResponseEntity<LoginResponseDTO.UserGetAccount> getAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        authentication.getAuthorities().forEach(grantedAuthority -> log.info("Authority: {}", grantedAuthority));
        return ResponseEntity.ok(this.userService.fetchAccount());
    }

    @GetMapping("/auth/refresh")
    @ApiMessage("Get user by refresh token")
    @Operation(summary = "Get user by refresh token", description = "Get user information using a refresh token and return a new refresh token.")
    public ResponseEntity<LoginResponseDTO> getRefreshToken(@CookieValue(name = "refresh_token") String refreshToken) {
        LoginResponseDTO res = this.userService.fetchUserByRefreshToken(refreshToken);

        String newRefreshToken = this.securityUtil.createRefreshToken(res.getUser().getEmail(), res);

        User savedUser = this.userService.handleGetUserByUsername(res.getUser().getEmail());
        if (savedUser != null) {
            this.userService.saveRefreshToken(savedUser, newRefreshToken);
        }

        ResponseCookie resCookie = ResponseCookie
                .from("refresh_token", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookie.toString()).body(res);
    }

    @PostMapping("/auth/logout")
    @ApiMessage("User logout")
    @Operation(summary = "User logout", description = "Logout the current user and clear the refresh token.")
    public ResponseEntity<Void> logout() {
        this.userService.handleUserLogout();
        ResponseCookie deleteSpringCookie = ResponseCookie
                .from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString()).body(null);
    }

    @PostMapping("/auth/change-password")
    @ApiMessage("User change password")
    @Operation(summary = "Change password", description = "Change the password of the currently logged-in user.")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequestDTO reqDTO) {
        this.userService.handleChangePassword(reqDTO);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/auth/outbound/authentication")
    public ResponseEntity<LoginResponseDTO> outboundAuthenticate(@RequestParam("code") String code) {
        LoginResponseDTO res = this.userService.outboundAuthenticate(code);

        String refreshToken = this.securityUtil.createRefreshToken(res.getUser().getEmail(), res);

        User user = userService.handleGetUserByUsername(res.getUser().getEmail());
        if (user != null) {
            // Lưu refresh token vào bảng tokens
            userService.saveRefreshToken(user, refreshToken);
        }

        ResponseCookie resCookie = ResponseCookie
                .from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookie.toString()).body(res);
    }

}
