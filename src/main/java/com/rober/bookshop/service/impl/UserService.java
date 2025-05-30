package com.rober.bookshop.service.impl;

import com.rober.bookshop.enums.TokenType;
import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.mapper.UserMapper;
import com.rober.bookshop.model.entity.Role;
import com.rober.bookshop.model.entity.Token;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.LoginRequestDTO;
import com.rober.bookshop.model.request.RegisterRequestDTO;
import com.rober.bookshop.model.request.UserRequestDTO;
import com.rober.bookshop.model.response.LoginResponseDTO;
import com.rober.bookshop.model.response.RegisterResponseDTO;
import com.rober.bookshop.model.response.ResultPaginationDTO;
import com.rober.bookshop.model.response.UserResponseDTO;
import com.rober.bookshop.repository.RoleRepository;
import com.rober.bookshop.repository.TokenRepository;
import com.rober.bookshop.repository.UserRepository;
import com.rober.bookshop.service.IEmailService;
import com.rober.bookshop.service.IUserService;
import com.rober.bookshop.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
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

    @Value("${rober.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @Override
    public User handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    @Override
    public RegisterResponseDTO register(RegisterRequestDTO requestDTO) {
        if (this.userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IdInvalidException("Email already exists");
        }

        User user = User.builder()
                .email(requestDTO.getEmail())
                .fullName(requestDTO.getFullName())
                .phone(requestDTO.getPhone())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .active(false)
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
        token.setExpiresAt(Instant.now().plusSeconds(15 * 60)); // 15 phút
        token.setCreatedAt(Instant.now());
        token.setRevoked(false);
        tokenRepository.save(token);

        // Gửi email xác minh
        String verifyLink = "http://localhost:8080" + "/api/v1/auth/verify?token=" + verifyToken;
        emailService.sendVerificationEmail(newUser.getEmail(), newUser.getFullName(), verifyLink);

        RegisterResponseDTO res = new RegisterResponseDTO();
        res.setId(newUser.getId());
        res.setEmail(newUser.getEmail());
        res.setPhone(newUser.getPhone());
        res.setFullName(newUser.getFullName());
        return res;
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
            LoginResponseDTO.UserLogin userLogin = LoginResponseDTO.UserLogin.builder()
                    .id(savedUser.getId())
                    .email(savedUser.getEmail())
                    .phone(savedUser.getPhone())
                    .address(savedUser.getAddress())
                    .fullName(savedUser.getFullName())
                    .avatar(savedUser.getAvatar())
                    .role(savedUser.getRole().getName())
                    .permissions(savedUser.getRole().getPermissions())
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
            userLogin.setRole(savedUser.getRole().getName());
            userLogin.setPermissions(savedUser.getRole().getPermissions());

            userGetAccount.setUser(userLogin);
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
            throw  new IdInvalidException("User not found for this refresh token");
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
    public void delete(Long id) {
        User deletedUser = this.userRepository.findById(id).orElseThrow(() -> new IdInvalidException("User with id = " + id + " not found"));
        deletedUser.setActive(false);
        this.userRepository.save(deletedUser);
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


}
