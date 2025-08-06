package com.rober.bookshop.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceTokenRequestDTO {
    @NotNull(message = "User ID is required")
    private Long userId;
    @NotBlank(message = "Device Token is required")
    private String deviceToken;
    @NotBlank(message = "Device Type is required")
    private String deviceType;

}
