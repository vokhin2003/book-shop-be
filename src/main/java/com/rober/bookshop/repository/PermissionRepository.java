package com.rober.bookshop.repository;

import com.rober.bookshop.model.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {

    boolean existsByPathAndMethodAndModule(String path, String method, String module);
    boolean existsByName(String name);
    List<Permission> findByIdIn(List<Long> ids);

}
