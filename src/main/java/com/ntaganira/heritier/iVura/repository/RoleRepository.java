package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    Optional<Role> findByName(String name);
    Optional<Role> findByCode(String code);

    @Query("select case when count(u) > 0 then true else false end from User u join u.roles r where r.id = :roleId")
    boolean isAssignedToUser(@Param("roleId") Long roleId);

    @Query("select count(u) from User u join u.roles r where r.id = :roleId")
    long countAssignedUsers(@Param("roleId") Long roleId);

    @Query("select case when count(r) > 0 then true else false end from Role r join r.permissions p where p.id = :permissionId")
    boolean isPermissionAssigned(@Param("permissionId") Long permissionId);
}
