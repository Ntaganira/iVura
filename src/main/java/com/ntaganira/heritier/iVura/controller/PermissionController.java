package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.PermissionDto;
import com.ntaganira.heritier.iVura.entity.Permission;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/permissions")
public class PermissionController {

    private static final int PAGE_SIZE = 10;

    private final PermissionService permissionService;
    private final ActivityLogService activityLogService;

    public PermissionController(PermissionService permissionService,
                                ActivityLogService activityLogService) {
        this.permissionService = permissionService;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_PERMISSION')")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String module,
                       Model model) {
        Page<com.ntaganira.heritier.iVura.entity.Permission> permissions =
                permissionService.findPage(search, module, page, PAGE_SIZE);
        model.addAttribute("permissions", permissions);
        model.addAttribute("modules", permissionService.findModules());
        model.addAttribute("search", search);
        model.addAttribute("module", module);
        model.addAttribute("paginationQuery", buildQuery(search, module));
        return "permissions/list";
    }

    private String buildQuery(String search, String module) {
        StringBuilder q = new StringBuilder();
        if (org.springframework.util.StringUtils.hasText(search)) {
            q.append("search=").append(java.net.URLEncoder.encode(search.trim(), java.nio.charset.StandardCharsets.UTF_8));
        }
        if (org.springframework.util.StringUtils.hasText(module)) {
            if (q.length() > 0) q.append('&');
            q.append("module=").append(java.net.URLEncoder.encode(module.trim(), java.nio.charset.StandardCharsets.UTF_8));
        }
        return q.toString();
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_PERMISSION')")
    public String addForm(Model model) {
        model.addAttribute("permissionDto", new PermissionDto());
        return "permissions/form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_PERMISSION')")
    public String editForm(@PathVariable Long id, Model model) {
        var permission = permissionService.findById(id);
        PermissionDto dto = new PermissionDto();
        dto.setId(permission.getId());
        dto.setCode(permission.getCode());
        dto.setName(permission.getName());
        dto.setModule(permission.getModule());
        dto.setAction(permission.getAction());
        dto.setDescription(permission.getDescription());
        dto.setEnabled(permission.isEnabled());
        model.addAttribute("permissionDto", dto);
        return "permissions/form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('PERM_CREATE_PERMISSION')")
    public String create(@Valid @ModelAttribute("permissionDto") PermissionDto dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("formErrors", result.getFieldErrors());
            return "permissions/form";
        }
        try {
            Permission permission = permissionService.create(dto);
            activityLogService.record("Permission Management", "CREATE_PERMISSION",
                    "Created permission " + permission.getCode(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Permission created successfully");
            return "redirect:/permissions";
        } catch (RuntimeException e) {
            activityLogService.record("Permission Management", "CREATE_PERMISSION",
                    "Failed to create permission " + dto.getCode(), ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/permissions/add";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_PERMISSION')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("permissionDto") PermissionDto dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("formErrors", result.getFieldErrors());
            return "permissions/form";
        }
        try {
            Permission permission = permissionService.update(id, dto);
            activityLogService.record("Permission Management", "UPDATE_PERMISSION",
                    "Updated permission " + permission.getCode(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Permission updated successfully");
            return "redirect:/permissions";
        } catch (RuntimeException e) {
            activityLogService.record("Permission Management", "UPDATE_PERMISSION",
                    "Failed to update permission " + dto.getCode(), ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/permissions/edit/" + id;
        }
    }

    @GetMapping("/toggle/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_PERMISSION')")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Permission permission = permissionService.toggleStatus(id);
        activityLogService.record("Permission Management", "UPDATE_PERMISSION",
                (permission.isEnabled() ? "Activated" : "Deactivated") + " permission " + permission.getCode(),
                ActivityStatus.SUCCESS);
        redirectAttributes.addFlashAttribute("flashSuccess",
                permission.isEnabled() ? "Permission activated" : "Permission deactivated");
        return "redirect:/permissions";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_DELETE_PERMISSION')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Permission permission = permissionService.findById(id);
            permissionService.delete(id);
            activityLogService.record("Permission Management", "DELETE_PERMISSION",
                    "Deleted permission " + permission.getCode(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Permission deleted successfully");
        } catch (RuntimeException e) {
            activityLogService.record("Permission Management", "DELETE_PERMISSION",
                    "Failed to delete permission #" + id, ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/permissions";
    }
}
