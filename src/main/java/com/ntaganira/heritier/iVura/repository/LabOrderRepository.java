package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.LabOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : LabOrderRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Laboratory Order Repository
 * </pre>
 */
public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {
    List<LabOrder> findAllByOrderByRequestedAtDesc();
    List<LabOrder> findByStatusOrderByRequestedAtDesc(String status);
    Optional<LabOrder> findByAccessionNumber(String accessionNumber);
    long countByStatus(String status);
}
