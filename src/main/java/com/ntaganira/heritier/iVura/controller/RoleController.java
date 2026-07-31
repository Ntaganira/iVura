package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.RoleDto;
import com.ntaganira.heritier.iVura.entity.AppPage;
import com.ntaganira.heritier.iVura.entity.Role;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.AppPageRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.PermissionService;
import com.ntaganira.heritier.iVura.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/roles")
public class RoleController {

    private static final int PAGE_SIZE = 8;

    private final RoleService roleService;
    private final PermissionService permissionService;
    private final AppPageRepository pageRepo;
    private final ActivityLogService activityLogService;

    public RoleController(RoleService roleService, PermissionService permissionService,
                          AppPageRepository pageRepo, ActivityLogService activityLogService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.pageRepo = pageRepo;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_ROLE')")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String status,
                       Model model) {
        Page<com.ntaganira.heritier.iVura.entity.Role> roles =
                roleService.findPage(search, status, page, PAGE_SIZE);
        model.addAttribute("roles", roles);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("paginationQuery", buildQuery(search, status));
        return "roles/list";
    }

    private String buildQuery(String search, String status) {
        StringBuilder q = new StringBuilder();
        if (org.springframework.util.StringUtils.hasText(search)) {
            q.append("search=").append(java.net.URLEncoder.encode(search.trim(), java.nio.charset.StandardCharsets.UTF_8));
        }
        if (org.springframework.util.StringUtils.hasText(status)) {
            if (q.length() > 0) q.append('&');
            q.append("status=").append(status.trim());
        }
        return q.toString();
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_ROLE')")
    public String addForm(Model model) {
        model.addAttribute("roleDto", new RoleDto());
        model.addAttribute("permissionsByModule", permissionService.findEnabledByModule());
        model.addAttribute("pagesByModule", groupPagesByModule());
        return "roles/form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_ROLE')")
    public String editForm(@PathVariable Long id, Model model) {
        var role = roleService.findById(id);
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setCode(role.getCode());
        dto.setDescription(role.getDescription());
        dto.setEnabled(role.isEnabled());
        if (role.getPermissions() != null) {
            role.getPermissions().forEach(p -> dto.getPermissionIds().add(p.getId()));
        }
        if (role.getPages() != null) {
            role.getPages().forEach(p -> dto.getPageIds().add(p.getId()));
        }
        model.addAttribute("roleDto", dto);
        model.addAttribute("permissionsByModule", permissionService.findEnabledByModule());
        model.addAttribute("pagesByModule", groupPagesByModule());
        return "roles/form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('PERM_CREATE_ROLE')")
    public String create(@Valid @ModelAttribute("roleDto") RoleDto dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("permissionsByModule", permissionService.findEnabledByModule());
            model.addAttribute("pagesByModule", groupPagesByModule());
            model.addAttribute("formErrors", result.getFieldErrors());
            return "roles/form";
        }
        try {
            Role role = roleService.create(dto);
            activityLogService.record("Role Management", "CREATE_ROLE",
                    "Created role " + role.getName(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Role created successfully");
            return "redirect:/roles";
        } catch (RuntimeException e) {
            activityLogService.record("Role Management", "CREATE_ROLE",
                    "Failed to create role " + dto.getName(), ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/roles/add";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_ROLE')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("roleDto") RoleDto dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("permissionsByModule", permissionService.findEnabledByModule());
            model.addAttribute("pagesByModule", groupPagesByModule());
            model.addAttribute("formErrors", result.getFieldErrors());
            return "roles/form";
        }
        try {
            Role role = roleService.update(id, dto);
            activityLogService.record("Role Management", "UPDATE_ROLE",
                    "Updated role " + role.getName(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Role updated successfully");
            return "redirect:/roles";
        } catch (RuntimeException e) {
            activityLogService.record("Role Management", "UPDATE_ROLE",
                    "Failed to update role " + dto.getName(), ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/roles/edit/" + id;
        }
    }

    @GetMapping("/toggle/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_ROLE')")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Role role = roleService.toggleStatus(id);
        activityLogService.record("Role Management", "UPDATE_ROLE",
                (role.isEnabled() ? "Activated" : "Deactivated") + " role " + role.getName(),
                ActivityStatus.SUCCESS);
        redirectAttributes.addFlashAttribute("flashSuccess",
                role.isEnabled() ? "Role activated" : "Role deactivated");
        return "redirect:/roles";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_DELETE_ROLE')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Role role = roleService.findById(id);
            roleService.delete(id);
            activityLogService.record("Role Management", "DELETE_ROLE",
                    "Deleted role " + role.getName(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Role deleted successfully");
        } catch (RuntimeException e) {
            activityLogService.record("Role Management", "DELETE_ROLE",
                    "Failed to delete role #" + id, ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/roles";
    }

    private Map<String, List<AppPage>> groupPagesByModule() {
        return pageRepo.findAllByOrderBySortOrderAsc()
                .stream()
                .collect(Collectors.groupingBy(AppPage::getModule, LinkedHashMap::new, Collectors.toList()));
    }
}
