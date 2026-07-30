package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.DoctorDto;
import com.ntaganira.heritier.iVura.repository.DepartmentRepository;
import com.ntaganira.heritier.iVura.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final DepartmentRepository departmentRepo;

    public DoctorController(DoctorService doctorService, DepartmentRepository departmentRepo) {
        this.doctorService = doctorService;
        this.departmentRepo = departmentRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("doctors", doctorService.findAll());
        return "doctors/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("doctorDto", new DoctorDto());
        model.addAttribute("departments", departmentRepo.findAll());
        return "doctors/add";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("doctorDto") DoctorDto dto,
                      BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentRepo.findAll());
            return "doctors/add";
        }
        doctorService.create(dto);
        return "redirect:/doctors";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        var doctor = doctorService.findById(id);
        DoctorDto dto = new DoctorDto();
        dto.setId(doctor.getId());
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setEmail(doctor.getEmail());
        dto.setPhone(doctor.getPhone());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setLicenseNumber(doctor.getLicenseNumber());
        dto.setDepartmentId(doctor.getDepartment() != null ? doctor.getDepartment().getId() : null);
        dto.setQualification(doctor.getQualification());
        dto.setExperienceYears(doctor.getExperienceYears());
        dto.setConsultationFee(doctor.getConsultationFee());
        dto.setAvailableFrom(doctor.getAvailableFrom());
        dto.setAvailableTo(doctor.getAvailableTo());
        model.addAttribute("doctorDto", dto);
        model.addAttribute("departments", departmentRepo.findAll());
        return "doctors/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("doctorDto") DoctorDto dto,
                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentRepo.findAll());
            return "doctors/edit";
        }
        doctorService.update(id, dto);
        return "redirect:/doctors";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        doctorService.delete(id);
        return "redirect:/doctors";
    }
}
