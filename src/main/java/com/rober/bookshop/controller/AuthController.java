package com.rober.bookshop.controller;

import com.rober.bookshop.annotation.ApiMessage;
import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.ChangePasswordRequestDTO;
import com.rober.bookshop.model.request.LoginRequestDTO;
import com.rober.bookshop.model.request.RegisterRequestDTO;
import com.rober.bookshop.model.response.LoginResponseDTO;
import com.rober.bookshop.model.response.RegisterResponseDTO;
import com.rober.bookshop.service.IEmailService;
import com.rober.bookshop.service.IFileService;
import com.rober.bookshop.service.IUserService;
import com.rober.bookshop.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
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
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO reqUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.register(reqUser));
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
    public ResponseEntity<Void> verifyUser(@RequestParam("token") String token) {
        try {
            this.userService.verifyUser(token);
            String redirectUrl = "http://localhost:3000/verify/return?status=success";
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();
        } catch (IdInvalidException e) {
            log.error("Xác minh thất bại: {}", e.getMessage());
            String redirectUrl = "http://localhost:3000/verify/return?status=error&message="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();
        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi xác minh: {}", e.getMessage());
            String redirectUrl = "http://localhost:3000/verify/return?status=error&message="
                    + URLEncoder.encode("Đã xảy ra lỗi không mong muốn", StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", redirectUrl)
                    .build();
        }
    }

    @PostMapping("/auth/login")
    @ApiMessage("User login")
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

    @GetMapping("/auth/account")
    @ApiMessage("Get user information")
    public ResponseEntity<LoginResponseDTO.UserGetAccount> getAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authentication.getAuthorities().forEach(grantedAuthority -> log.info("Authority: {}", grantedAuthority));
        return ResponseEntity.ok(this.userService.fetchAccount());
    }

    @GetMapping("/auth/refresh")
    @ApiMessage("Get user by refresh token")
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
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequestDTO reqDTO) {
        this.userService.handleChangePassword(reqDTO);
        return ResponseEntity.ok(null);
    }

}
