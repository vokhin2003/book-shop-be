package com.rober.bookshop.service.impl;

import com.rober.bookshop.exception.IdInvalidException;
import com.rober.bookshop.model.entity.UserDeviceToken;
import com.rober.bookshop.model.request.DeviceTokenRequestDTO;
import com.rober.bookshop.repository.UserDeviceTokenRepository;
import com.rober.bookshop.service.IUserDeviceTokenService;
import com.rober.bookshop.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDeviceTokenService implements IUserDeviceTokenService {

    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final IUserService userService;

    @Override
    public void saveDeviceToken(DeviceTokenRequestDTO request) {
        if (request.getUserId() == null || request.getDeviceToken() == null || request.getDeviceType() == null) {
            throw new IdInvalidException("Bad request");
        }

        UserDeviceToken existingToken = userDeviceTokenRepository
                .findByUserIdAndDeviceTokenAndDeviceType(
                        request.getUserId(),
                        request.getDeviceToken(),
                        request.getDeviceType()
                ).orElse(null);

        if (existingToken == null) {
            UserDeviceToken token = new UserDeviceToken();
            token.setUser(userService.getUserById(request.getUserId()));
            token.setDeviceToken(request.getDeviceToken());
            token.setDeviceType(request.getDeviceType());
            token.setActive(true);
            userDeviceTokenRepository.save(token);
        } else {
            existingToken.setActive(true);
            userDeviceTokenRepository.save(existingToken);
        }
    }

    @Override
    @Transactional
    public void removeDeviceToken(DeviceTokenRequestDTO request) {
        if (request.getUserId() == null || request.getDeviceToken() == null || request.getDeviceType() == null) {
            throw new IdInvalidException("Bad request");
        }

        userDeviceTokenRepository.deleteByUserIdAndDeviceTokenAndDeviceType(request.getUserId(), request.getDeviceToken(), request.getDeviceType());
    }
}
