package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.DepartmentDto;
import com.ntaganira.heritier.iVura.entity.Department;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private static final int PAGE_SIZE = 10;

    private final DepartmentService departmentService;
    private final DoctorRepository doctorRepo;
    private final ActivityLogService activityLogService;

    public DepartmentController(DepartmentService departmentService, DoctorRepository doctorRepo,
                                ActivityLogService activityLogService) {
        this.departmentService = departmentService;
        this.doctorRepo = doctorRepo;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_DEPARTMENT')")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String search,
                       Model model) {
        Page<Department> departments = departmentService.findPage(search, page, PAGE_SIZE);
        model.addAttribute("departments", departments);
        model.addAttribute("search", search);
        model.addAttribute("doctorCount", departmentService.countDoctors());
        model.addAttribute("doctorCounts", departmentService.doctorCounts());
        model.addAttribute("paginationQuery", buildQuery(search));
        return "departments/list";
    }

    private String buildQuery(String search) {
        if (org.springframework.util.StringUtils.hasText(search)) {
            return "search=" + URLEncoder.encode(search.trim(), StandardCharsets.UTF_8);
        }
        return "";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_DEPARTMENT')")
    public String addForm(Model model) {
        model.addAttribute("departmentDto", new DepartmentDto());
        return "departments/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_DEPARTMENT')")
    public String create(@Valid @ModelAttribute("departmentDto") DepartmentDto dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("formErrors", result.getFieldErrors());
            return "departments/form";
        }
        try {
            Department department = departmentService.create(dto);
            activityLogService.record("Department Management", "CREATE_DEPARTMENT",
                    "Created department " + department.getName(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Department created successfully");
            return "redirect:/departments";
        } catch (RuntimeException e) {
            activityLogService.record("Department Management", "CREATE_DEPARTMENT",
                    "Failed to create department " + dto.getName(), ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/departments/add";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_DEPARTMENT')")
    public String editForm(@PathVariable Long id, Model model) {
        Department department = departmentService.findById(id);
        DepartmentDto dto = new DepartmentDto();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        dto.setPhone(department.getPhone());
        dto.setLocation(department.getLocation());
        model.addAttribute("departmentDto", dto);
        return "departments/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_DEPARTMENT')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("departmentDto") DepartmentDto dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("formErrors", result.getFieldErrors());
            return "departments/form";
        }
        try {
            Department department = departmentService.update(id, dto);
            activityLogService.record("Department Management", "UPDATE_DEPARTMENT",
                    "Updated department " + department.getName(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Department updated successfully");
            return "redirect:/departments";
        } catch (RuntimeException e) {
            activityLogService.record("Department Management", "UPDATE_DEPARTMENT",
                    "Failed to update department " + dto.getName(), ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/departments/edit/" + id;
        }
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAuthority('PERM_VIEW_DEPARTMENT')")
    public String view(@PathVariable Long id, Model model) {
        Department department = departmentService.findById(id);
        model.addAttribute("department", department);
        model.addAttribute("doctors", doctorRepo.findByDepartmentId(id));
        return "departments/view";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_DELETE_DEPARTMENT')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Department department = departmentService.findById(id);
            departmentService.delete(id);
            activityLogService.record("Department Management", "DELETE_DEPARTMENT",
                    "Deleted department " + department.getName(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Department deleted successfully");
        } catch (RuntimeException e) {
            activityLogService.record("Department Management", "DELETE_DEPARTMENT",
                    "Failed to delete department #" + id, ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/departments";
    }
}
