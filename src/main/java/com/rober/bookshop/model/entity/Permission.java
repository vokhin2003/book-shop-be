package com.rober.bookshop.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "permissions")
@Getter
@Setter
public class Permission extends Base {

    @Column(unique = true, nullable = false, length = 50)
    private String name;
    @Column(nullable = false)
    private String path;
    @Column(nullable = false)
    private String method;
    @Column(nullable = false)
    private String module;

    @ManyToMany(mappedBy = "permissions")
    @JsonIgnore
    private List<Role> roles;
}
