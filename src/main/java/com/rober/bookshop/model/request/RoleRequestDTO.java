package com.rober.bookshop.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RoleRequestDTO {

    @NotBlank(message = "Role name is required")
    private String name;
    private String description;
    private List<Long> permissionIds;

}
