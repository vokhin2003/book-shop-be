package com.rober.bookshop.model.request;

import com.rober.bookshop.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequestDTO {
    @NotBlank
    private String fullName;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String province;

    @NotBlank
    private String ward;

    @NotBlank
    private String addressDetail;

    @NotNull
    private AddressType addressType;

    private boolean isDefault;
}


