package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.LabTestCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : LabTestCatalogRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Lab Test Catalog Repository
 * </pre>
 */
public interface LabTestCatalogRepository extends JpaRepository<LabTestCatalog, Long> {
    List<LabTestCatalog> findByIsActiveTrueOrderByNameAsc();
    Optional<LabTestCatalog> findByCode(String code);
}
