package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.SpecializationDto;
import com.ntaganira.heritier.iVura.entity.Specialization;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.SpecializationService;
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
@RequestMapping("/specializations")
public class SpecializationController {

    private static final int PAGE_SIZE = 10;

    private final SpecializationService specializationService;
    private final ActivityLogService activityLogService;

    public SpecializationController(SpecializationService specializationService,
                                    ActivityLogService activityLogService) {
        this.specializationService = specializationService;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_SPECIALIZATION')")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String search,
                       Model model) {
        Page<Specialization> specializations = specializationService.findPage(search, page, PAGE_SIZE);
        model.addAttribute("specializations", specializations);
        model.addAttribute("search", search);
        model.addAttribute("doctorCount", specializationService.countDoctors());
        model.addAttribute("doctorCounts", specializationService.doctorCounts());
        model.addAttribute("paginationQuery", buildQuery(search));
        return "specializations/list";
    }

    private String buildQuery(String search) {
        if (org.springframework.util.StringUtils.hasText(search)) {
            return "search=" + URLEncoder.encode(search.trim(), StandardCharsets.UTF_8);
        }
        return "";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_SPECIALIZATION')")
    public String addForm(Model model) {
        model.addAttribute("specializationDto", new SpecializationDto());
        return "specializations/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_SPECIALIZATION')")
    public String create(@Valid @ModelAttribute("specializationDto") SpecializationDto dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("formErrors", result.getFieldErrors());
            return "specializations/form";
        }
        try {
            Specialization specialization = specializationService.create(dto);
            activityLogService.record("Specialization Management", "CREATE_SPECIALIZATION",
                    "Created specialization " + specialization.getName(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Specialization created successfully");
            return "redirect:/specializations";
        } catch (RuntimeException e) {
            activityLogService.record("Specialization Management", "CREATE_SPECIALIZATION",
                    "Failed to create specialization " + dto.getName(), ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/specializations/add";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_SPECIALIZATION')")
    public String editForm(@PathVariable Long id, Model model) {
        Specialization specialization = specializationService.findById(id);
        SpecializationDto dto = new SpecializationDto();
        dto.setId(specialization.getId());
        dto.setName(specialization.getName());
        dto.setDescription(specialization.getDescription());
        model.addAttribute("specializationDto", dto);
        return "specializations/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_SPECIALIZATION')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("specializationDto") SpecializationDto dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("formErrors", result.getFieldErrors());
            return "specializations/form";
        }
        try {
            Specialization specialization = specializationService.update(id, dto);
            activityLogService.record("Specialization Management", "UPDATE_SPECIALIZATION",
                    "Updated specialization " + specialization.getName(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Specialization updated successfully");
            return "redirect:/specializations";
        } catch (RuntimeException e) {
            activityLogService.record("Specialization Management", "UPDATE_SPECIALIZATION",
                    "Failed to update specialization " + dto.getName(), ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/specializations/edit/" + id;
        }
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_DELETE_SPECIALIZATION')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Specialization specialization = specializationService.findById(id);
            specializationService.delete(id);
            activityLogService.record("Specialization Management", "DELETE_SPECIALIZATION",
                    "Deleted specialization " + specialization.getName(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Specialization deleted successfully");
        } catch (RuntimeException e) {
            activityLogService.record("Specialization Management", "DELETE_SPECIALIZATION",
                    "Failed to delete specialization #" + id, ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/specializations";
    }
}
