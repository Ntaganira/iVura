package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Dispensation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : DispensationRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Dispensation Repository
 * </pre>
 */
public interface DispensationRepository extends JpaRepository<Dispensation, Long> {
    List<Dispensation> findByPatientIdOrderByDispensedAtDesc(Long patientId);
    List<Dispensation> findAllByOrderByDispensedAtDesc();
}
