package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByIsActiveTrue();
    long countByIsActiveTrue();

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.createdAt BETWEEN :from AND :to")
    long countCreatedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
