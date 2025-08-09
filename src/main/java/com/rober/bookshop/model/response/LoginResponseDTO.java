package com.rober.bookshop.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rober.bookshop.model.entity.Permission;
import lombok.*;

import java.util.List;

@Getter
@Setter
public class LoginResponseDTO {

    @JsonProperty("access_token")
    private String accessToken;

    private UserLogin user;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserLogin {
        private String email;
        private String phone;
        private String fullName;
        private String address;
        private String role;
        private Long id;
        private String avatar;
        private List<Permission> permissions;
        private Boolean noPassword;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInsideToken {
        private Long id;
        private String email;
        private String fullName;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserGetAccount {
        private UserLogin user;
    }


}
