package com.rober.bookshop.service.impl;

import com.rober.bookshop.enums.TokenType;
import com.rober.bookshop.exception.ForbiddenException;
import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.exception.UnauthorizedException;
import com.rober.bookshop.exception.InputInvalidException;
import com.rober.bookshop.mapper.UserMapper;
import com.rober.bookshop.model.entity.Role;
import com.rober.bookshop.model.entity.Token;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.*;
import com.rober.bookshop.model.response.*;
import com.rober.bookshop.repository.RoleRepository;
import com.rober.bookshop.repository.TokenRepository;
import com.rober.bookshop.repository.UserRepository;
import com.rober.bookshop.repository.httpclient.OutboundIdentityClient;
import com.rober.bookshop.repository.httpclient.OutboundUserClient;
import com.rober.bookshop.service.IEmailService;
import com.rober.bookshop.service.IUserService;
import com.rober.bookshop.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;
    private final TokenRepository tokenRepository;
    private final IEmailService emailService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final OutboundIdentityClient outboundIdentityClient;
    private final OutboundUserClient outboundUserClient;

    @Value("${rober.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;


    @Value("${outbound.identity.client-id}")
    private String CLIENT_ID;

    @Value("${outbound.identity.client-secret}")
    private String CLIENT_SECRET;

    @Value("${outbound.identity.redirect-uri}")
    private String REDIRECT_URI;

    @NonFinal
    private final String GRANT_TYPE = "authorization_code";

    @Value("${rober.server.ip}")
    private String serverIp;


    @Override
    public User handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    @Override
    public RegisterResponseDTO register(RegisterRequestDTO requestDTO, String clientPlatform) {
        if (this.userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IdInvalidException("Email already exists");
        }

        User user = User.builder()
                .email(requestDTO.getEmail())
                .fullName(requestDTO.getFullName())
                .phone(requestDTO.getPhone())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .active(false)
                .adminActive(true)
                .build();

        Role userRole = roleRepository.findByName("CUSTOMER");
        user.setRole(userRole);

        User newUser = this.userRepository.save(user);

        String verifyToken = securityUtil.generateVerifyToken(newUser.getEmail());
        log.info("verifyToken: {}", verifyToken);

        Token token = new Token();
        token.setUser(newUser);
        token.setToken(verifyToken);
        token.setType(TokenType.VERIFY);
        token.setExpiresAt(Instant.now().plusSeconds(60)); // TEST: 1 phút
        token.setCreatedAt(Instant.now());
        token.setRevoked(false);
        tokenRepository.save(token);

        // Gửi email xác minh


        String verifyLink = clientPlatform.equalsIgnoreCase("web") ?
                "http://localhost:8080"
                :
                "http://" + serverIp + ":8080";

        verifyLink +=  "/api/v1/auth/verify?token=" + verifyToken + "&clientPlatform=" + clientPlatform;


        emailService.sendVerificationEmail(newUser.getEmail(), newUser.getFullName(), verifyLink);

        RegisterResponseDTO res = new RegisterResponseDTO();
        res.setId(newUser.getId());
        res.setEmail(newUser.getEmail());
        res.setPhone(newUser.getPhone());
        res.setFullName(newUser.getFullName());
        return res;
    }

    @Override
    public void resendVerification(String email, String clientPlatform) {
        User user = this.userRepository.findByEmail(email);
        if (user == null) {
            throw new IdInvalidException("User not found with email");
        }
        if (user.isActive()) {
            throw new IdInvalidException("User already verified");
        }

        // Revoke all previous VERIFY tokens of this user
        this.tokenRepository.findByUserAndTypeAndRevokedFalseAndExpiresAtAfter(user, TokenType.VERIFY, Instant.now())
                .forEach(t -> {
                    t.setRevoked(true);
                    tokenRepository.save(t);
                    log.info("token with id = {} has been revoked", t.getId());
                });

        String verifyToken = securityUtil.generateVerifyToken(user.getEmail());
        Token token = new Token();
        token.setUser(user);
        token.setToken(verifyToken);
        token.setType(TokenType.VERIFY);
        token.setExpiresAt(Instant.now().plusSeconds(60)); // TEST: 1 phút
        token.setCreatedAt(Instant.now());
        token.setRevoked(false);
        tokenRepository.save(token);

        String verifyLink = clientPlatform.equalsIgnoreCase("web") ?
                "http://localhost:8080" :
                "http://" + serverIp + ":8080";
        verifyLink += "/api/v1/auth/verify?token=" + verifyToken + "&clientPlatform=" + clientPlatform;

        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verifyLink);
    }

    @Override
    public void verifyUser(String token) {
        Token verificationToken = this.tokenRepository.findByToken(token);
        if (verificationToken == null) {
            throw new IdInvalidException("Invalid verification token");
        }

        if (verificationToken.getType() != TokenType.VERIFY) {
            throw new IdInvalidException("Token is not for verification");
        }

        if (verificationToken.isRevoked()) {
            throw new IdInvalidException("Token has been revoked");
        }

        if (Instant.now().isAfter(verificationToken.getExpiresAt())) {
            throw new IdInvalidException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        if (user == null) {
            throw new IdInvalidException("User not found for this token");
        }

        if (user.isActive()) {
            throw new IdInvalidException("User is already verified");
        }

        user.setActive(true);
        user.setVerifiedBy("EMAIL");
        User newUser = userRepository.save(user);

        // Thu hồi token sau khi sử dụng
        verificationToken.setRevoked(true);
        tokenRepository.save(verificationToken);

        log.info("User with id {} has been active = {}", newUser.getId(), newUser.isActive());

        log.info("User with email {} has been verified successfully", user.getEmail());
    }

    @Override
    public LoginResponseDTO handleUserLogin(LoginRequestDTO reqLoginDTO) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(reqLoginDTO.getUsername(), reqLoginDTO.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        LoginResponseDTO res = new LoginResponseDTO();

        User savedUser = handleGetUserByUsername(reqLoginDTO.getUsername());
        if (savedUser != null) {
            if (!savedUser.isActive()) {
                throw new UnauthorizedException("Account is not verified");
            }
            if (!savedUser.isAdminActive()) {
                throw new ForbiddenException("Account disabled by admin");
            }
            LoginResponseDTO.UserLogin userLogin = LoginResponseDTO.UserLogin.builder()
                    .id(savedUser.getId())
                    .email(savedUser.getEmail())
                    .phone(savedUser.getPhone())
                    .address(savedUser.getAddress())
                    .fullName(savedUser.getFullName())
                    .avatar(savedUser.getAvatar())
                    .role(savedUser.getRole().getName())
                    .permissions(savedUser.getRole().getPermissions())
                    .noPassword(!StringUtils.hasText(savedUser.getPassword()))
                    .build();
            res.setUser(userLogin);
        }

        String accessToken = this.securityUtil.createAccessToken(reqLoginDTO.getUsername(), res);

        res.setAccessToken(accessToken);

        return res;
    }

    @Override
    @Transactional
    public void saveRefreshToken(User user, String refreshToken) {
        if (user == null) {
            throw new IdInvalidException("User not found for saving refresh token");
        }

//        this.tokenRepository.findByUserAndTypeAndRevokedFalseAndExpiresAtAfter(user, TokenType.REFRESH, Instant.now())
//                .forEach(token -> {
//                    token.setRevoked(true);
//                    tokenRepository.save(token);
//                    log.info("Revoked old refresh token: {} for user: {}", token.getToken(), user.getEmail());
//                });

        log.info("refreshToken is {}", refreshToken);

        this.tokenRepository.findByUserAndTypeAndRevokedFalseAndExpiresAtAfter(user, TokenType.REFRESH, Instant.now())
                .stream()
                .filter(token -> !token.getToken().equals(refreshToken))
                .forEach(token -> {
                    token.setRevoked(true);
                    tokenRepository.save(token);
                    log.info("Revoked old refresh token: {} for user: {}", token.getToken(), user.getEmail());
                });


        // Kiểm tra xem token đã tồn tại chưa
        if (tokenRepository.findByToken(refreshToken) != null) {
            log.info("Refresh token already exists for user: {}", user.getEmail());
            return; // Không lưu nếu token đã tồn tại
        }

        Token token = Token.builder()
                .token(refreshToken)
                .type(TokenType.REFRESH)
                .expiresAt(Instant.now().plusSeconds(refreshTokenExpiration))
                .createdAt(Instant.now())
                .revoked(false)
                .user(user)
                .build();
        this.tokenRepository.save(token);
        log.info("Refresh token saved for user with email: {}", user.getEmail());
        log.info("refresh token is saved in database {}", refreshToken);
    }

    @Override
    public LoginResponseDTO.UserGetAccount fetchAccount() {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        User savedUser = handleGetUserByUsername(email);
        LoginResponseDTO.UserLogin userLogin = new LoginResponseDTO.UserLogin();
        LoginResponseDTO.UserGetAccount userGetAccount = new LoginResponseDTO.UserGetAccount();

        if (savedUser != null) {
            userLogin.setId(savedUser.getId());
            userLogin.setEmail(savedUser.getEmail());
            userLogin.setFullName(savedUser.getFullName());
            userLogin.setPhone(savedUser.getPhone());
            userLogin.setAddress(savedUser.getAddress());
            userLogin.setAvatar(savedUser.getAvatar());
            userLogin.setRole(savedUser.getRole().getName());
            userLogin.setPermissions(savedUser.getRole().getPermissions());
            userLogin.setNoPassword(!StringUtils.hasText(savedUser.getPassword()));

            userGetAccount.setUser(userLogin);
        } else {
            throw new UnauthorizedException("User not found for this token");
        }

        return userGetAccount;
    }

    @Override
    public LoginResponseDTO fetchUserByRefreshToken(String refreshToken) {
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();

        Token token = this.tokenRepository.findByToken(refreshToken);
        if (token == null) {
            throw new IdInvalidException("Refresh token not found in database");
        }

        if (token.isRevoked()) {
            throw new IdInvalidException("Refresh token has been revoked");
        }

        if (Instant.now().isAfter(token.getExpiresAt())) {
            throw new IdInvalidException("Refresh token has expired");
        }

        User savedUser = token.getUser();
        if (savedUser == null) {
            throw new IdInvalidException("User not found for this refresh token");
        }

        LoginResponseDTO res = new LoginResponseDTO();
        LoginResponseDTO.UserLogin userLogin = LoginResponseDTO.UserLogin.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .fullName(savedUser.getFullName())
                .avatar(savedUser.getAvatar())
                .role(savedUser.getRole().getName())
                .permissions(savedUser.getRole().getPermissions())
                .noPassword(!StringUtils.hasText(savedUser.getPassword()))
                .build();
        res.setUser(userLogin);

        String accessToken = this.securityUtil.createAccessToken(email, res);
        res.setAccessToken(accessToken);
        return res;
    }

    @Override
    public UserResponseDTO create(UserRequestDTO reqDTO) {
        if (this.userRepository.existsByEmail(reqDTO.getEmail())) {
            throw new IdInvalidException("Email already exists.");
        }

        Role role = this.roleRepository.findById(reqDTO.getRole()).orElseThrow(() -> new IdInvalidException("Role with id = " + reqDTO.getRole() + " not found"));

        User user = userMapper.toUser(reqDTO);
        user.setRole(role);
        user.setPassword(this.passwordEncoder.encode(reqDTO.getPassword()));

        return this.userMapper.toResponseDTO(this.userRepository.save(user));
    }

    @Override
    public UserResponseDTO update(Long id, UserRequestDTO reqDTO) {
        User updatedUser = this.userRepository.findById(id).orElseThrow(() -> new IdInvalidException("User with id = " + id + " not found"));

        Role role = this.roleRepository.findById(reqDTO.getRole()).orElseThrow(() -> new IdInvalidException("Role with id = " + reqDTO.getRole() + " not found"));

        this.userMapper.updateUserFromDTO(reqDTO, updatedUser);
        updatedUser.setRole(role);

        return this.userMapper.toResponseDTO(this.userRepository.save(updatedUser));
    }

    @Override
    public UserResponseDTO toggleAdminActive(Long id, boolean isAdminActive) {
        User user = this.userRepository.findById(id).orElseThrow(() -> new IdInvalidException("User with id = " + id + " not found"));
        user.setAdminActive(isAdminActive);
        return this.userMapper.toResponseDTO(this.userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        User deletedUser = this.userRepository.findById(id).orElseThrow(() -> new IdInvalidException("User with id = " + id + " not found"));
        deletedUser.setActive(false);
        this.userRepository.save(deletedUser);
    }

    @Override
    public UserResponseDTO handleUpdateInfo(UserInfoRequestDTO reqDTO) {
        User updatedUser = this.userRepository.findById(reqDTO.getId()).orElseThrow(() -> new IdInvalidException("User with id = " + reqDTO.getId() + " not found in database"));

        this.userMapper.updateUserFromInfoDTO(reqDTO, updatedUser);
        return this.userMapper.toResponseDTO(this.userRepository.save(updatedUser));
    }

    @Override
    public ResultPaginationDTO getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setCurrent(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());

        meta.setPages(pageUser.getTotalPages());
        meta.setTotal(pageUser.getTotalElements());

        res.setMeta(meta);

        List<UserResponseDTO> listUser = pageUser.getContent().stream().map(this.userMapper::toResponseDTO).toList();

        res.setResult(listUser);

        return res;
    }

    @Override
    @Transactional
    public void handleUserLogout() {
        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(() -> new IdInvalidException("No user found in SecurityContext for logout"));

        User user = handleGetUserByUsername(email);
        if (user == null) {
            log.warn("User with email {} not found for logout", email);
            SecurityContextHolder.clearContext();
            return;
        }

        this.tokenRepository.findByUserAndTypeAndRevokedFalseAndExpiresAtAfter(user, TokenType.REFRESH, Instant.now())
                .forEach(token -> {
                    token.setRevoked(true);
                    tokenRepository.save(token);
                    log.info("Refresh token {} revoked for user {}", token.getToken(), email);
                });

        SecurityContextHolder.clearContext();
        log.info("User session cleared for email {}", email);
    }

    @Override
    public User getUserLogin() {
        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(() -> new IdInvalidException("User not found in SecurityContext"));

        return handleGetUserByUsername(email);
    }

    @Override
    public void handleChangePassword(ChangePasswordRequestDTO reqDTO) {
        User user = this.userRepository.findById(reqDTO.getId()).orElseThrow(() -> new IdInvalidException("User with id = " + reqDTO.getId() + " not found in database"));

        if (!passwordEncoder.matches(reqDTO.getOldPassword(), user.getPassword())) {
            throw new InputInvalidException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(reqDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {

        User userDB = userRepository.findById(id).orElseThrow(() -> new IdInvalidException("User with id = " + id + " not found"));
        return userDB;

    }

    @Override
    public LoginResponseDTO outboundAuthenticate(String code) {
        ExchangeTokenResponseDTO response = outboundIdentityClient.exchangeToken(ExchangeTokenRequestDTO.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());

        log.info("TOKEN RESPONSE {}", response);

        // Get user info
        OutboundUserResponseDTO userInfo = outboundUserClient.getUserInfo("json", response.getAccessToken());

        LoginResponseDTO res = new LoginResponseDTO();
        Role userRole = roleRepository.findByName("CUSTOMER");


        // Onboard user
        User user = handleGetUserByUsername(userInfo.getEmail());
        if (user == null) {
            String fullName = userInfo.getGivenName() + " " + userInfo.getFamilyName();
            user = userRepository.save(User.builder()
                    .email(userInfo.getEmail())
                    .fullName(fullName)
                    .role(userRole)
                    .active(true)
                    .verifiedBy("GOOGLE")
                    .adminActive(true)
                    .build());
        }
        // User exists but not active yet: trust Google verified email -> activate, unless admin disabled
        if (user != null) {
            if (!user.isAdminActive()) {
                throw new ForbiddenException("Account disabled by admin");
            }
            if (!user.isActive() && userInfo.isVerifiedEmail()) {
                user.setActive(true);
                user.setVerifiedBy("GOOGLE");
                user = userRepository.save(user);
            }
        }

        LoginResponseDTO.UserLogin userLogin = LoginResponseDTO.UserLogin.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .fullName(user.getFullName())
                .avatar(user.getAvatar())
                .role(user.getRole().getName())
                .permissions(user.getRole().getPermissions())
                .noPassword(!StringUtils.hasText(user.getPassword()))
                .build();

        res.setUser(userLogin);
        String accessToken = this.securityUtil.createAccessToken(userInfo.getEmail(), res);
        res.setAccessToken(accessToken);
        return res;
    }

    @Override
    public void createPassword(CreatePasswordRequestDTO request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InputInvalidException("Password must be equal confirm password");
        }

        User user = getUserLogin();
        log.info(">>> Password of user: {}", user.getPassword());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    @Override
    public void handleForgotPassword(ForgotPasswordRequestDTO request, String clientPlatform) {
        User user = this.userRepository.findByEmail(request.getEmail());
        if (user == null) {
            throw new IdInvalidException("Email không tồn tại trong hệ thống");
        }
        if (!user.isAdminActive()) {
            throw new IdInvalidException("Tài khoản đã bị khoá bởi quản trị viên");
        }
        // tạo/reset token RESET_PASSWORD
        this.tokenRepository.findByUserAndTypeAndRevokedFalseAndExpiresAtAfter(user, TokenType.RESET_PASSWORD, Instant.now())
                .forEach(t -> {
                    t.setRevoked(true);
                    tokenRepository.save(t);
                });

        String jwt = securityUtil.generateResetToken(user.getEmail());
        Token token = new Token();
        token.setUser(user);
        token.setToken(jwt);
        token.setType(TokenType.RESET_PASSWORD);
        token.setExpiresAt(Instant.now().plusSeconds(15 * 60));
        token.setCreatedAt(Instant.now());
        token.setRevoked(false);
        tokenRepository.save(token);

        String resetLink = (clientPlatform.equalsIgnoreCase("web") ? "http://localhost:8080" : "http://" + serverIp + ":8080")
                + "/api/v1/auth/reset?token=" + jwt;

        resetLink+= "&clientPlatform=" + clientPlatform;

        // bạn có thể triển khai template riêng, tạm gọi hàm default
        emailService.sendResetPasswordEmail(user.getEmail(), user.getFullName(), resetLink);
    }

    @Override
    public String handleResetRedirect(String token, String clientPlatform) {
        Token t = tokenRepository.findByToken(token);

        boolean isWeb = clientPlatform.equalsIgnoreCase("web");

        if (t == null || t.isRevoked() || t.getType() != TokenType.RESET_PASSWORD || Instant.now().isAfter(t.getExpiresAt())) {
            return isWeb?
                    "http://localhost:3000/forgot/return?status=error"
                    :
                    "http://bromel.free.nf/forgot-return.html?status=error"; // FE sẽ render lỗi
        }
        // token hợp lệ: redirect về FE cùng token để render form
        return isWeb ?
                "http://localhost:3000/forgot/return?status=success&token=" + token
                :
                "http://bromel.free.nf/forgot-return.html?status=success&token=" + token;

    }

    @Override
    public void handleResetPassword(ResetPasswordRequestDTO request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InputInvalidException("Password must be equal confirm password");
        }
        Token t = tokenRepository.findByToken(request.getToken());
        if (t == null || t.isRevoked() || t.getType() != TokenType.RESET_PASSWORD || Instant.now().isAfter(t.getExpiresAt())) {
            throw new IdInvalidException("Invalid reset token");
        }
        User user = t.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        // revoke token sau khi đổi mật khẩu
        t.setRevoked(true);
        tokenRepository.save(t);
    }

}
