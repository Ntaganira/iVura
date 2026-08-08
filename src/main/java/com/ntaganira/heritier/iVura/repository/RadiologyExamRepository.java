package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.RadiologyExam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : RadiologyExamRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Radiology Exam Catalog Repository
 * </pre>
 */
public interface RadiologyExamRepository extends JpaRepository<RadiologyExam, Long> {
    List<RadiologyExam> findByIsActiveTrueOrderByNameAsc();
    Optional<RadiologyExam> findByCode(String code);
}
