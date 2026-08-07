package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.LabResultDto;
import com.ntaganira.heritier.iVura.entity.LabResult;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.LabResultService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : LaboratoryController.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Laboratory Controller
 * </pre>
 */
@Controller
@RequestMapping("/laboratory")
public class LaboratoryController {

    private final LabResultService labService;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final ActivityLogService activityLogService;

    public LaboratoryController(LabResultService labService,
                                PatientRepository patientRepo,
                                DoctorRepository doctorRepo,
                                ActivityLogService activityLogService) {
        this.labService = labService;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_LAB')")
    public String list(@RequestParam(required = false) String status, Model model) {
        model.addAttribute("labs", labService.findByStatus(status));
        model.addAttribute("status", status);
        model.addAttribute("statuses", LabResultService.STATUSES);
        model.addAttribute("counts", labService.statusCounts());
        return "laboratory/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_LAB')")
    public String addForm(@RequestParam(required = false) Long patientId, Model model) {
        LabResultDto dto = new LabResultDto();
        if (patientId != null) {
            dto.setPatientId(patientId);
        }
        model.addAttribute("labDto", dto);
        model.addAttribute("patients", patientRepo.findByIsActiveTrue());
        model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
        model.addAttribute("statuses", LabResultService.STATUSES);
        return "laboratory/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_LAB')")
    public String add(@Valid @ModelAttribute("labDto") LabResultDto dto,
                      BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formErrors", result.getFieldErrors());
            model.addAttribute("patients", patientRepo.findByIsActiveTrue());
            model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
            model.addAttribute("statuses", LabResultService.STATUSES);
            return "laboratory/form";
        }
        LabResult lab = labService.create(dto);
        activityLogService.record("Laboratory", "CREATE_LAB",
                "Recorded " + lab.getTestName() + " for " + lab.getPatient().getFullName(),
                ActivityStatus.SUCCESS);
        return "redirect:/laboratory";
    }

    @GetMapping("/status/{id}/{status}")
    @PreAuthorize("hasAuthority('PERM_EDIT_LAB')")
    public String updateStatus(@PathVariable Long id, @PathVariable String status) {
        LabResult lab = labService.updateStatus(id, status);
        activityLogService.record("Laboratory", "EDIT_LAB",
                "Lab " + lab.getTestName() + " for " + lab.getPatient().getFullName()
                        + " marked " + lab.getStatus(), ActivityStatus.SUCCESS);
        return "redirect:/laboratory";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_LAB')")
    public String editForm(@PathVariable Long id, Model model) {
        LabResult lab = labService.findById(id);
        LabResultDto dto = new LabResultDto();
        dto.setId(lab.getId());
        dto.setPatientId(lab.getPatient().getId());
        dto.setDoctorId(lab.getDoctor() != null ? lab.getDoctor().getId() : null);
        dto.setTestName(lab.getTestName());
        dto.setCategory(lab.getCategory());
        dto.setResult(lab.getResult());
        dto.setUnit(lab.getUnit());
        dto.setNormalRange(lab.getNormalRange());
        dto.setStatus(lab.getStatus());
        dto.setNotes(lab.getNotes());
        model.addAttribute("labDto", dto);
        model.addAttribute("patients", patientRepo.findByIsActiveTrue());
        model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
        model.addAttribute("statuses", LabResultService.STATUSES);
        return "laboratory/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_LAB')")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("labDto") LabResultDto dto,
                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formErrors", result.getFieldErrors());
            model.addAttribute("patients", patientRepo.findByIsActiveTrue());
            model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
            model.addAttribute("statuses", LabResultService.STATUSES);
            return "laboratory/form";
        }
        LabResult lab = labService.update(id, dto);
        activityLogService.record("Laboratory", "EDIT_LAB",
                "Updated lab " + lab.getTestName() + " for " + lab.getPatient().getFullName(),
                ActivityStatus.SUCCESS);
        return "redirect:/laboratory";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_LAB')")
    public String delete(@PathVariable Long id) {
        LabResult lab = labService.findById(id);
        labService.delete(id);
        activityLogService.record("Laboratory", "EDIT_LAB",
                "Deleted lab " + lab.getTestName(), ActivityStatus.SUCCESS);
        return "redirect:/laboratory";
    }
}
