package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.PermissionDto;
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

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
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
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "permissions/form";
        }
        try {
            permissionService.create(dto);
            redirectAttributes.addFlashAttribute("flashSuccess", "Permission created successfully");
            return "redirect:/permissions";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/permissions/add";
        }
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_PERMISSION')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("permissionDto") PermissionDto dto,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "permissions/form";
        }
        try {
            permissionService.update(id, dto);
            redirectAttributes.addFlashAttribute("flashSuccess", "Permission updated successfully");
            return "redirect:/permissions";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/permissions/edit/" + id;
        }
    }

    @GetMapping("/toggle/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_PERMISSION')")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var permission = permissionService.toggleStatus(id);
        redirectAttributes.addFlashAttribute("flashSuccess",
                permission.isEnabled() ? "Permission activated" : "Permission deactivated");
        return "redirect:/permissions";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_DELETE_PERMISSION')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            permissionService.delete(id);
            redirectAttributes.addFlashAttribute("flashSuccess", "Permission deleted successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/permissions";
    }
}
