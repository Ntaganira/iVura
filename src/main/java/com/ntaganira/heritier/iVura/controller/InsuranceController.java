package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.InsuranceClaimDto;
import com.ntaganira.heritier.iVura.entity.InsuranceClaim;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.BillingRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.InsuranceClaimService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : InsuranceController.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Insurance claims controller
 * </pre>
 */
@Controller
@RequestMapping("/insurance")
public class InsuranceController {

    private static final int PAGE_SIZE = 10;

    private final InsuranceClaimService claimService;
    private final PatientRepository patientRepo;
    private final BillingRepository billingRepo;
    private final ActivityLogService activityLogService;

    public InsuranceController(InsuranceClaimService claimService,
                               PatientRepository patientRepo,
                               BillingRepository billingRepo,
                               ActivityLogService activityLogService) {
        this.claimService = claimService;
        this.patientRepo = patientRepo;
        this.billingRepo = billingRepo;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_CLAIM')")
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String status,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<InsuranceClaim> claims = claimService.findPage(search, status, page, PAGE_SIZE);
        model.addAttribute("claims", claims);
        model.addAttribute("statuses", InsuranceClaimService.STATUSES);
        model.addAttribute("statusCounts", claimService.statusCounts());
        model.addAttribute("totalClaimed", claimService.totalClaimed());
        model.addAttribute("paginationQuery", buildQuery(search, status));
        return "insurance/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_CLAIM')")
    public String addForm(Model model) {
        model.addAttribute("claimDto", new InsuranceClaimDto());
        model.addAttribute("patients", patientRepo.findByIsActiveTrue());
        model.addAttribute("bills", billingRepo.findAll());
        return "insurance/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_CLAIM')")
    public String add(@Valid @ModelAttribute("claimDto") InsuranceClaimDto dto,
                      BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("patients", patientRepo.findByIsActiveTrue());
            model.addAttribute("bills", billingRepo.findAll());
            model.addAttribute("formErrors", result.getFieldErrors());
            return "insurance/form";
        }
        InsuranceClaim claim = claimService.create(dto);
        activityLogService.record("Insurance", "CREATE_CLAIM",
                "Submitted claim " + claim.getClaimNumber() + " for " + claim.getPatient().getFullName(),
                ActivityStatus.SUCCESS);
        ra.addFlashAttribute("flashSuccess", "Claim " + claim.getClaimNumber() + " submitted");
        return "redirect:/insurance";
    }

    @PostMapping("/status/{id}")
    @PreAuthorize("hasAuthority('PERM_APPROVE_CLAIM')")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               @RequestParam(required = false) String remarks,
                               RedirectAttributes ra) {
        InsuranceClaim claim = claimService.updateStatus(id, status, remarks);
        activityLogService.record("Insurance", "APPROVE_CLAIM",
                "Claim " + claim.getClaimNumber() + " set to " + claim.getStatus(),
                ActivityStatus.SUCCESS);
        ra.addFlashAttribute("flashSuccess", "Claim " + claim.getClaimNumber() + " marked " + claim.getStatus());
        return "redirect:/insurance";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_CLAIM')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        InsuranceClaim claim = claimService.findById(id);
        claimService.delete(id);
        activityLogService.record("Insurance", "DELETE_CLAIM",
                "Deleted claim " + claim.getClaimNumber(), ActivityStatus.SUCCESS);
        ra.addFlashAttribute("flashSuccess", "Claim deleted");
        return "redirect:/insurance";
    }

    private String buildQuery(String search, String status) {
        StringBuilder sb = new StringBuilder();
        if (search != null && !search.isBlank()) {
            sb.append("&search=").append(search.trim());
        }
        if (status != null && !status.isBlank()) {
            sb.append("&status=").append(status.trim());
        }
        return sb.toString();
    }
}
