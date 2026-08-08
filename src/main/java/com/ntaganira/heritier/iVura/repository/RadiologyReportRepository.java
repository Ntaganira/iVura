package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.RadiologyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : RadiologyReportRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Radiology Report Repository
 * </pre>
 */
public interface RadiologyReportRepository extends JpaRepository<RadiologyReport, Long> {
    Optional<RadiologyReport> findByOrderItemId(Long orderItemId);
    List<RadiologyReport> findByStatusOrderByCreatedAtAsc(String status);
}
