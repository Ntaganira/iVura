package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.InsuranceClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : InsuranceClaimRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Insurance Claim Repository
 * </pre>
 */
public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, Long>,
        JpaSpecificationExecutor<InsuranceClaim> {
    List<InsuranceClaim> findByPatientIdOrderBySubmittedDateDesc(Long patientId);
    InsuranceClaim findTopByOrderByIdDesc();
    long countByStatus(String status);
}
