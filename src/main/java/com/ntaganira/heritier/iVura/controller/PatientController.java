package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.PatientDto;
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

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
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
                      BindingResult result) {
        if (result.hasErrors()) {
            return "patients/add";
        }
        patientService.create(dto);
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
                       BindingResult result) {
        if (result.hasErrors()) {
            return "patients/edit";
        }
        patientService.update(id, dto);
        return "redirect:/patients";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_DELETE_PATIENT')")
    public String delete(@PathVariable Long id) {
        patientService.delete(id);
        return "redirect:/patients";
    }
}
