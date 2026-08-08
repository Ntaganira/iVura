package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.entity.LabTestCatalog;
import com.ntaganira.heritier.iVura.service.LabCatalogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : LabCatalogController.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Laboratory Test Catalog Controller
 * </pre>
 */
@Controller
@RequestMapping("/lab-catalog")
public class LabCatalogController {

    private final LabCatalogService catalogService;

    public LabCatalogController(LabCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_CATALOG')")
    public String list(Model model) {
        model.addAttribute("tests", catalogService.findAll());
        return "lab-catalog/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_MANAGE_CATALOG')")
    public String addForm(Model model) {
        model.addAttribute("catalog", new LabTestCatalog());
        return "lab-catalog/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_MANAGE_CATALOG')")
    public String add(@ModelAttribute LabTestCatalog catalog, RedirectAttributes ra) {
        catalogService.save(catalog);
        ra.addFlashAttribute("flashSuccess", "Test " + catalog.getCode() + " added");
        return "redirect:/lab-catalog";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_MANAGE_CATALOG')")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("catalog", catalogService.findById(id));
        return "lab-catalog/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_MANAGE_CATALOG')")
    public String edit(@PathVariable Long id, @ModelAttribute LabTestCatalog catalog, RedirectAttributes ra) {
        LabTestCatalog existing = catalogService.findById(id);
        if (existing == null) {
            ra.addFlashAttribute("flashError", "Test not found");
            return "redirect:/lab-catalog";
        }
        existing.setCode(catalog.getCode());
        existing.setName(catalog.getName());
        existing.setCategory(catalog.getCategory());
        existing.setSpecimenType(catalog.getSpecimenType());
        existing.setUnit(catalog.getUnit());
        existing.setNormalRange(catalog.getNormalRange());
        existing.setRefLow(catalog.getRefLow());
        existing.setRefHigh(catalog.getRefHigh());
        existing.setCriticalLow(catalog.getCriticalLow());
        existing.setCriticalHigh(catalog.getCriticalHigh());
        existing.setDeltaThreshold(catalog.getDeltaThreshold());
        existing.setAutoVerifyEligible(catalog.getAutoVerifyEligible());
        existing.setIsActive(catalog.getIsActive() != null ? catalog.getIsActive() : true);
        catalogService.save(existing);
        ra.addFlashAttribute("flashSuccess", "Test " + existing.getCode() + " updated");
        return "redirect:/lab-catalog";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_MANAGE_CATALOG')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        catalogService.delete(id);
        ra.addFlashAttribute("flashSuccess", "Test deactivated");
        return "redirect:/lab-catalog";
    }
}
