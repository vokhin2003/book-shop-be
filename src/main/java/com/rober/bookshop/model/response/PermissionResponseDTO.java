package com.rober.bookshop.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class PermissionResponseDTO {

    private Long id;
    private String name;
    private String path;
    private String method;
    private String module;
    private Instant createdAt;
    private Instant updatedAt;

}
