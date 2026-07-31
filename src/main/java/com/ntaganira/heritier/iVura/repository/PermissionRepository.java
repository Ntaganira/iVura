package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    Optional<Permission> findByCode(String code);
    List<Permission> findByEnabledTrueOrderByModuleAscActionAsc();
    List<Permission> findAllByOrderByModuleAscActionAsc();
}
