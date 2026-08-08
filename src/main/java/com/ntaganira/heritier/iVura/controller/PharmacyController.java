package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.entity.Dispensation;
import com.ntaganira.heritier.iVura.entity.Medicine;
import com.ntaganira.heritier.iVura.entity.User;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import com.ntaganira.heritier.iVura.repository.UserRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.PharmacyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : PharmacyController.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Pharmacy inventory and dispensing controller
 * </pre>
 */
@Controller
@RequestMapping("/pharmacy")
public class PharmacyController {

    private final PharmacyService pharmacyService;
    private final PatientRepository patientRepo;
    private final UserRepository userRepo;
    private final ActivityLogService activityLogService;

    public PharmacyController(PharmacyService pharmacyService,
                              PatientRepository patientRepo,
                              UserRepository userRepo,
                              ActivityLogService activityLogService) {
        this.pharmacyService = pharmacyService;
        this.patientRepo = patientRepo;
        this.userRepo = userRepo;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_MEDICINE')")
    public String index(Model model) {
        model.addAttribute("medicines", pharmacyService.findAllMedicines());
        model.addAttribute("lowStock", pharmacyService.lowStockMedicines());
        model.addAttribute("dispensations", pharmacyService.allDispensations().stream().limit(10).toList());
        model.addAttribute("patients", patientRepo.findByIsActiveTrue());
        return "pharmacy/index";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_MANAGE_MEDICINE')")
    public String addForm(Model model) {
        model.addAttribute("medicine", new Medicine());
        return "pharmacy/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_MANAGE_MEDICINE')")
    public String add(@ModelAttribute Medicine medicine, RedirectAttributes ra) {
        Medicine saved = pharmacyService.saveMedicine(medicine, null);
        activityLogService.record("Pharmacy", "MANAGE_MEDICINE",
                "Added medicine " + saved.getName(), ActivityStatus.SUCCESS);
        ra.addFlashAttribute("flashSuccess", "Medicine " + saved.getName() + " added");
        return "redirect:/pharmacy";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_MANAGE_MEDICINE')")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("medicine", pharmacyService.findMedicine(id));
        return "pharmacy/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_MANAGE_MEDICINE')")
    public String edit(@PathVariable Long id, @ModelAttribute Medicine medicine, RedirectAttributes ra) {
        Medicine saved = pharmacyService.saveMedicine(medicine, id);
        activityLogService.record("Pharmacy", "MANAGE_MEDICINE",
                "Updated medicine " + saved.getName(), ActivityStatus.SUCCESS);
        ra.addFlashAttribute("flashSuccess", "Medicine " + saved.getName() + " updated");
        return "redirect:/pharmacy";
    }

    @PostMapping("/stock/{id}")
    @PreAuthorize("hasAuthority('PERM_MANAGE_MEDICINE')")
    public String adjustStock(@PathVariable Long id,
                              @RequestParam int adjustment,
                              RedirectAttributes ra) {
        Medicine saved = pharmacyService.adjustStock(id, adjustment);
        ra.addFlashAttribute("flashSuccess", "Stock for " + saved.getName()
                + " adjusted to " + saved.getStockQuantity());
        return "redirect:/pharmacy";
    }

    @PostMapping("/dispense")
    @PreAuthorize("hasAuthority('PERM_DISPENSE_MEDICINE')")
    public String dispense(@RequestParam Long patientId,
                           @RequestParam Long medicineId,
                           @RequestParam int quantity,
                           @RequestParam(required = false) String note,
                           RedirectAttributes ra) {
        try {
            Dispensation d = pharmacyService.dispense(patientId, medicineId, quantity, note, currentUser());
            activityLogService.record("Pharmacy", "DISPENSE_MEDICINE",
                    "Dispensed " + d.getQuantity() + " of " + d.getMedicine().getName()
                            + " to " + d.getPatient().getFullName(),
                    ActivityStatus.SUCCESS);
            ra.addFlashAttribute("flashSuccess", "Dispensed " + d.getQuantity() + " of "
                    + d.getMedicine().getName());
        } catch (RuntimeException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/pharmacy";
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? userRepo.findByUsername(auth.getName()).orElse(null) : null;
    }
}
