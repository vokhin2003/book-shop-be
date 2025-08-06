package com.rober.bookshop.service;

import com.rober.bookshop.model.request.DeviceTokenRequestDTO;

public interface IUserDeviceTokenService {

    void saveDeviceToken(DeviceTokenRequestDTO request);
    void removeDeviceToken(DeviceTokenRequestDTO request);

}
