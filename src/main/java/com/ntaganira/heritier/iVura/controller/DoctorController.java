package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.DoctorDto;
import com.ntaganira.heritier.iVura.entity.Doctor;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.DepartmentRepository;
import com.ntaganira.heritier.iVura.repository.ServiceRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final DepartmentRepository departmentRepo;
    private final ServiceRepository serviceRepo;
    private final ActivityLogService activityLogService;

    public DoctorController(DoctorService doctorService, DepartmentRepository departmentRepo,
                            ServiceRepository serviceRepo,
                            ActivityLogService activityLogService) {
        this.doctorService = doctorService;
        this.departmentRepo = departmentRepo;
        this.serviceRepo = serviceRepo;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_DOCTOR')")
    public String list(Model model) {
        model.addAttribute("doctors", doctorService.findAll());
        return "doctors/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_DOCTOR')")
    public String addForm(Model model) {
        model.addAttribute("doctorDto", new DoctorDto());
        model.addAttribute("departments", departmentRepo.findAll());
        model.addAttribute("services", serviceRepo.findAllByOrderByNameAsc());
        return "doctors/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_DOCTOR')")
    public String add(@Valid @ModelAttribute("doctorDto") DoctorDto dto,
                      BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentRepo.findAll());
            model.addAttribute("services", serviceRepo.findAllByOrderByNameAsc());
            model.addAttribute("formErrors", result.getFieldErrors());
            return "doctors/add";
        }
        Doctor doctor = doctorService.create(dto);
        activityLogService.record("Doctor Management", "CREATE_DOCTOR",
                "Created doctor " + doctor.getFullName(), ActivityStatus.SUCCESS);
        return "redirect:/doctors";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_DOCTOR')")
    public String editForm(@PathVariable Long id, Model model) {
        var doctor = doctorService.findById(id);
        DoctorDto dto = new DoctorDto();
        dto.setId(doctor.getId());
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setEmail(doctor.getEmail());
        dto.setPhone(doctor.getPhone());
        dto.setServiceId(doctor.getService() != null ? doctor.getService().getId() : null);
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setDepartmentId(doctor.getDepartment() != null ? doctor.getDepartment().getId() : null);
        dto.setQualification(doctor.getQualification());
        dto.setExperienceYears(doctor.getExperienceYears());
        dto.setConsultationFee(doctor.getConsultationFee());
        dto.setAvailableFrom(doctor.getAvailableFrom());
        dto.setAvailableTo(doctor.getAvailableTo());
        model.addAttribute("doctorDto", dto);
        model.addAttribute("departments", departmentRepo.findAll());
        model.addAttribute("services", serviceRepo.findAllByOrderByNameAsc());
        return "doctors/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_DOCTOR')")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("doctorDto") DoctorDto dto,
                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentRepo.findAll());
            model.addAttribute("services", serviceRepo.findAllByOrderByNameAsc());
            model.addAttribute("formErrors", result.getFieldErrors());
            return "doctors/edit";
        }
        Doctor doctor = doctorService.update(id, dto);
        activityLogService.record("Doctor Management", "UPDATE_DOCTOR",
                "Updated doctor " + doctor.getFullName(), ActivityStatus.SUCCESS);
        return "redirect:/doctors";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_DELETE_DOCTOR')")
    public String delete(@PathVariable Long id) {
        Doctor doctor = doctorService.findById(id);
        doctorService.delete(id);
        activityLogService.record("Doctor Management", "DELETE_DOCTOR",
                "Deleted doctor " + doctor.getFullName(), ActivityStatus.SUCCESS);
        return "redirect:/doctors";
    }
}
