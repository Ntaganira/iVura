package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByIsActiveTrue();
    List<Doctor> findByDepartmentId(Long departmentId);
    long countByIsActiveTrue();
}
