package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {
    Optional<Department> findByNameIgnoreCase(String name);
}
