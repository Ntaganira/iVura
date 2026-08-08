package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.AnalyzerDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : AnalyzerDeviceRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Analyzer Device Repository
 * </pre>
 */
public interface AnalyzerDeviceRepository extends JpaRepository<AnalyzerDevice, Long> {
    List<AnalyzerDevice> findByIsActiveTrueOrderByNameAsc();
    List<AnalyzerDevice> findAllByOrderByNameAsc();
    Optional<AnalyzerDevice> findByCode(String code);
}
