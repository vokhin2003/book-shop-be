package com.rober.bookshop.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDTO {

    @Email(message = "Email is invalid")
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Password is required")
    private String password;
    @NotBlank(message = "FullName is required")
    private String fullName;
    private String address;
    private String phone;
    private boolean active = true;
    private String avatar;
    @NotNull(message = "Role is required")
    private Long role;

}
