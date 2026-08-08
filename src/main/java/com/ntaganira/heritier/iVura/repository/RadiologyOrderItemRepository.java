package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.RadiologyOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : RadiologyOrderItemRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Radiology Order Item Repository
 * </pre>
 */
public interface RadiologyOrderItemRepository extends JpaRepository<RadiologyOrderItem, Long> {
    List<RadiologyOrderItem> findByOrderIdOrderByIdAsc(Long orderId);
    List<RadiologyOrderItem> findByStatusOrderByIdAsc(String status);
    long countByOrderIdAndStatus(Long orderId, String status);
    long countByOrderId(Long orderId);
}
