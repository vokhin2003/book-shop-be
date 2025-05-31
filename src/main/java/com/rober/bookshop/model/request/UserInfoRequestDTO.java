package com.rober.bookshop.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoRequestDTO {

    @NotNull(message = "User id is required")
    Long id;
    @NotBlank(message = "FullName is required")
    private String fullName;
    @NotBlank(message = "Address is required")
    private String address;
    @NotBlank(message = "Phone is required")
    private String phone;
    private String avatar;

}
