package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.LabOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : LabOrderItemRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Laboratory Order Item Repository
 * </pre>
 */
public interface LabOrderItemRepository extends JpaRepository<LabOrderItem, Long> {
    List<LabOrderItem> findByOrderIdOrderByIdAsc(Long orderId);
    long countByOrderIdAndStatus(Long orderId, String status);
    long countByOrderId(Long orderId);
}
