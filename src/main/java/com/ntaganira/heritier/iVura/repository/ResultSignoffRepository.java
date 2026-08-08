package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.ResultSignoff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : ResultSignoffRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Lab Result Sign-off Repository
 * </pre>
 */
public interface ResultSignoffRepository extends JpaRepository<ResultSignoff, Long> {
    List<ResultSignoff> findByLabResultIdOrderByCreatedAtDesc(Long labResultId);
}
