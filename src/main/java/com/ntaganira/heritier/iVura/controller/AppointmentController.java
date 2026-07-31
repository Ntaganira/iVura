package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.AppointmentDto;
import com.ntaganira.heritier.iVura.enums.AppointmentStatus;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import com.ntaganira.heritier.iVura.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;

    public AppointmentController(AppointmentService appointmentService,
                                  PatientRepository patientRepo,
                                  DoctorRepository doctorRepo) {
        this.appointmentService = appointmentService;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_APPOINTMENT')")
    public String list(Model model) {
        model.addAttribute("appointments", appointmentService.findAll());
        return "appointments/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_APPOINTMENT')")
    public String addForm(Model model) {
        model.addAttribute("appointmentDto", new AppointmentDto());
        model.addAttribute("patients", patientRepo.findByIsActiveTrue());
        model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
        return "appointments/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_APPOINTMENT')")
    public String add(@Valid @ModelAttribute("appointmentDto") AppointmentDto dto,
                      BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("patients", patientRepo.findByIsActiveTrue());
            model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
            return "appointments/add";
        }
        appointmentService.create(dto);
        return "redirect:/appointments";
    }

    @GetMapping("/status/{id}/{status}")
    @PreAuthorize("hasAuthority('PERM_EDIT_APPOINTMENT')")
    public String updateStatus(@PathVariable Long id, @PathVariable String status) {
        appointmentService.updateStatus(id, status);
        return "redirect:/appointments";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_CANCEL_APPOINTMENT')")
    public String delete(@PathVariable Long id) {
        appointmentService.delete(id);
        return "redirect:/appointments";
    }
}
