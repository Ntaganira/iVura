package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.PatientDto;
import com.ntaganira.heritier.iVura.entity.Patient;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;
    private final ActivityLogService activityLogService;

    public PatientController(PatientService patientService, ActivityLogService activityLogService) {
        this.patientService = patientService;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_PATIENT')")
    public String list(Model model) {
        model.addAttribute("patients", patientService.findAll());
        return "patients/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_PATIENT')")
    public String addForm(Model model) {
        model.addAttribute("patientDto", new PatientDto());
        return "patients/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_PATIENT')")
    public String add(@Valid @ModelAttribute("patientDto") PatientDto dto,
                      BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formErrors", result.getFieldErrors());
            return "patients/add";
        }
        Patient patient = patientService.create(dto);
        activityLogService.record("Patient Management", "CREATE_PATIENT",
                "Created patient " + patient.getFullName(), ActivityStatus.SUCCESS);
        return "redirect:/patients";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_PATIENT')")
    public String editForm(@PathVariable Long id, Model model) {
        var patient = patientService.findById(id);
        PatientDto dto = new PatientDto();
        dto.setId(patient.getId());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setEmail(patient.getEmail());
        dto.setPhone(patient.getPhone());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setBloodGroup(patient.getBloodGroup());
        dto.setAddress(patient.getAddress());
        dto.setCity(patient.getCity());
        dto.setState(patient.getState());
        dto.setZipCode(patient.getZipCode());
        dto.setEmergencyContactName(patient.getEmergencyContactName());
        dto.setEmergencyContactPhone(patient.getEmergencyContactPhone());
        dto.setMedicalHistory(patient.getMedicalHistory());
        dto.setAllergies(patient.getAllergies());
        model.addAttribute("patientDto", dto);
        return "patients/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_PATIENT')")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("patientDto") PatientDto dto,
                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formErrors", result.getFieldErrors());
            return "patients/edit";
        }
        Patient patient = patientService.update(id, dto);
        activityLogService.record("Patient Management", "UPDATE_PATIENT",
                "Updated patient " + patient.getFullName(), ActivityStatus.SUCCESS);
        return "redirect:/patients";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_DELETE_PATIENT')")
    public String delete(@PathVariable Long id) {
        Patient patient = patientService.findById(id);
        patientService.delete(id);
        activityLogService.record("Patient Management", "DELETE_PATIENT",
                "Deleted patient " + patient.getFullName(), ActivityStatus.SUCCESS);
        return "redirect:/patients";
    }
}
