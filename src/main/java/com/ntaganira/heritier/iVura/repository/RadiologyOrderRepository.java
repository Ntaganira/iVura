package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.RadiologyOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : RadiologyOrderRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Radiology Order Repository
 * </pre>
 */
public interface RadiologyOrderRepository extends JpaRepository<RadiologyOrder, Long> {
    List<RadiologyOrder> findAllByOrderByRequestedAtDesc();
    List<RadiologyOrder> findByStatusOrderByRequestedAtDesc(String status);
    Optional<RadiologyOrder> findByAccessionNumber(String accessionNumber);
    long countByStatus(String status);
}
