package com.rober.bookshop.service.impl;

import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.mapper.UserMapper;
import com.rober.bookshop.model.entity.Role;
import com.rober.bookshop.model.entity.User;
import com.rober.bookshop.model.request.*;
import com.rober.bookshop.model.response.*;
import com.rober.bookshop.repository.RoleRepository;
import com.rober.bookshop.repository.UserRepository;
import com.rober.bookshop.service.IUserService;
import com.rober.bookshop.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    


    @Override
    public User handleGetUserByUsername(String username) {
        return this.userRepository.findByEmail(username).orElse(null);
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
    public User getUserLogin() {
        String email = SecurityUtil.getCurrentUserLogin().orElseThrow(() -> new IdInvalidException("User not found in SecurityContext"));

        return handleGetUserByUsername(email);
    }

    

    @Override
    public User getUserById(Long id) {

        User userDB = userRepository.findById(id).orElseThrow(() -> new IdInvalidException("User with id = " + id + " not found"));
        return userDB;

    }



}
