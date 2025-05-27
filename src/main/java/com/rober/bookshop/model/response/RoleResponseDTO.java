package com.rober.bookshop.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class RoleResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private List<PermissionResponseDTO> permissions;

}
