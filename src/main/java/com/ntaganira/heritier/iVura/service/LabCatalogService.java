package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.entity.LabTestCatalog;
import com.ntaganira.heritier.iVura.repository.LabTestCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.service
 * - File      : LabCatalogService.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Laboratory Test Catalog Service
 * </pre>
 */
@Service
public class LabCatalogService {

    private final LabTestCatalogRepository catalogRepo;

    public LabCatalogService(LabTestCatalogRepository catalogRepo) {
        this.catalogRepo = catalogRepo;
    }

    public List<LabTestCatalog> findAll() {
        return catalogRepo.findAll();
    }

    public List<LabTestCatalog> findActive() {
        return catalogRepo.findByIsActiveTrueOrderByNameAsc();
    }

    public LabTestCatalog findById(Long id) {
        return catalogRepo.findById(id).orElse(null);
    }

    @Transactional
    public LabTestCatalog save(LabTestCatalog catalog) {
        return catalogRepo.save(catalog);
    }

    @Transactional
    public void delete(Long id) {
        LabTestCatalog catalog = findById(id);
        if (catalog != null) {
            catalog.setIsActive(false);
            catalogRepo.save(catalog);
        }
    }
}
