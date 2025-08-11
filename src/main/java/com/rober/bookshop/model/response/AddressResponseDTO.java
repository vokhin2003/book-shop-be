package com.rober.bookshop.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rober.bookshop.enums.AddressType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class AddressResponseDTO {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String province;
    private String ward;
    private String addressDetail;
    private AddressType addressType;
    @JsonProperty("is_default")
    private boolean isDefault;
    private Instant createdAt;
    private Instant updatedAt;
}


