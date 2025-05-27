package com.rober.bookshop.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterResponseDTO {

    private Long id;
    private String email;
    private String fullName;
    private String phone;

}
