package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : MedicineRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Medicine Repository
 * </pre>
 */
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByIsActiveTrueOrderByNameAsc();
    List<Medicine> findByStockQuantityLessThanEqual(Integer reorderLevel);
}
