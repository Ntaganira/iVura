package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.ServiceDto;
import com.ntaganira.heritier.iVura.entity.Service;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.ServiceService;
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
@RequestMapping("/services")
public class ServiceController {

    private static final int PAGE_SIZE = 10;

    private final ServiceService serviceService;
    private final DoctorRepository doctorRepo;
    private final ActivityLogService activityLogService;

    public ServiceController(ServiceService serviceService, DoctorRepository doctorRepo,
                             ActivityLogService activityLogService) {
        this.serviceService = serviceService;
        this.doctorRepo = doctorRepo;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_SERVICE')")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String search,
                       Model model) {
        Page<Service> services = serviceService.findPage(search, page, PAGE_SIZE);
        model.addAttribute("services", services);
        model.addAttribute("search", search);
        model.addAttribute("doctorCount", serviceService.countDoctors());
        model.addAttribute("doctorCounts", serviceService.doctorCounts());
        model.addAttribute("paginationQuery", buildQuery(search));
        return "services/list";
    }

    private String buildQuery(String search) {
        if (org.springframework.util.StringUtils.hasText(search)) {
            return "search=" + URLEncoder.encode(search.trim(), StandardCharsets.UTF_8);
        }
        return "";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_SERVICE')")
    public String addForm(Model model) {
        model.addAttribute("serviceDto", new ServiceDto());
        return "services/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_SERVICE')")
    public String create(@Valid @ModelAttribute("serviceDto") ServiceDto dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("formErrors", result.getFieldErrors());
            return "services/form";
        }
        try {
            Service service = serviceService.create(dto);
            activityLogService.record("Service Management", "CREATE_SERVICE",
                    "Created service " + service.getName(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Service created successfully");
            return "redirect:/services";
        } catch (RuntimeException e) {
            activityLogService.record("Service Management", "CREATE_SERVICE",
                    "Failed to create service " + dto.getName(), ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/services/add";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_SERVICE')")
    public String editForm(@PathVariable Long id, Model model) {
        Service service = serviceService.findById(id);
        ServiceDto dto = new ServiceDto();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setPrice(service.getPrice());
        model.addAttribute("serviceDto", dto);
        return "services/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_SERVICE')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("serviceDto") ServiceDto dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("formErrors", result.getFieldErrors());
            return "services/form";
        }
        try {
            Service service = serviceService.update(id, dto);
            activityLogService.record("Service Management", "UPDATE_SERVICE",
                    "Updated service " + service.getName(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Service updated successfully");
            return "redirect:/services";
        } catch (RuntimeException e) {
            activityLogService.record("Service Management", "UPDATE_SERVICE",
                    "Failed to update service " + dto.getName(), ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/services/edit/" + id;
        }
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_DELETE_SERVICE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Service service = serviceService.findById(id);
            serviceService.delete(id);
            activityLogService.record("Service Management", "DELETE_SERVICE",
                    "Deleted service " + service.getName(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Service deleted successfully");
        } catch (RuntimeException e) {
            activityLogService.record("Service Management", "DELETE_SERVICE",
                    "Failed to delete service #" + id, ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/services";
    }
}
