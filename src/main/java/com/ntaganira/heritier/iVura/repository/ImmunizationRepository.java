package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Immunization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : ImmunizationRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Immunization Repository
 * </pre>
 */
public interface ImmunizationRepository extends JpaRepository<Immunization, Long> {
    List<Immunization> findByPatientIdOrderByAdministeredDateDesc(Long patientId);
    List<Immunization> findAllByOrderByAdministeredDateDesc();
}
