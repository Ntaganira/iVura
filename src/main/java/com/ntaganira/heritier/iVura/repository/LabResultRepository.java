package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.LabResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : LabResultRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Laboratory Result Repository
 * </pre>
 */
public interface LabResultRepository extends JpaRepository<LabResult, Long> {
    List<LabResult> findByPatientId(Long patientId);
    List<LabResult> findByPatientIdOrderByPerformedAtDesc(Long patientId);
    List<LabResult> findByStatusOrderByPerformedAtDesc(String status);
    List<LabResult> findAllByOrderByPerformedAtDesc();
    long countByStatus(String status);
    List<LabResult> findBySourceAndStatusInAndPublishedAtIsNullOrderByPerformedAtAsc(String source, java.util.List<String> statuses);
    List<LabResult> findByAccessionNumberOrderByIdAsc(String accessionNumber);
    long countBySourceAndPublishedAtIsNull(String source);
}
