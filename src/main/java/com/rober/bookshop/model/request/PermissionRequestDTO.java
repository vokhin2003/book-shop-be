package com.rober.bookshop.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionRequestDTO {

    @NotBlank(message = "Permission name is required")
    private String name;
    @NotBlank(message = "Path is required")
    private String path;
    @NotBlank(message = "Method is required")
    private String method;
    @NotBlank(message = "Module is required")
    private String module;

}
