package com.rober.bookshop.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UserResponseDTO {

    private Long id;
    private String email;
    private String fullName;
    private String address;
    private String phone;
    private boolean active;
    private boolean adminActive;
    private String verifiedBy;
    private String avatar;
    private RoleUser role;

    private Instant createdAt;
    private Instant updatedAt;

    // For future FE usage if needed
    // private List<AddressResponseDTO> addresses;

    @Getter
    @Setter
    public static class RoleUser {
        private Long id;
        private String name;
    }

}
