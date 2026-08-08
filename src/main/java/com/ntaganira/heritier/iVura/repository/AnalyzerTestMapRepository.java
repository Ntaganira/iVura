package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.AnalyzerTestMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : AnalyzerTestMapRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Analyzer Test Map Repository
 * </pre>
 */
public interface AnalyzerTestMapRepository extends JpaRepository<AnalyzerTestMap, Long> {
    List<AnalyzerTestMap> findByDeviceId(Long deviceId);
    Optional<AnalyzerTestMap> findByDeviceIdAndDeviceTestCode(Long deviceId, String deviceTestCode);
}
