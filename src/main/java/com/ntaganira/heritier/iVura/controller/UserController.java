package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.UserDto;
import com.ntaganira.heritier.iVura.repository.RoleRepository;
import com.ntaganira.heritier.iVura.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {

    private static final int PAGE_SIZE = 8;

    private final UserService userService;
    private final RoleRepository roleRepo;

    public UserController(UserService userService, RoleRepository roleRepo) {
        this.userService = userService;
        this.roleRepo = roleRepo;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_USER')")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String status,
                       Model model) {
        Page<com.ntaganira.heritier.iVura.entity.User> users =
                userService.findPage(search, status, page, PAGE_SIZE);
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("paginationQuery", buildQuery(search, status));
        return "users/list";
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
    @PreAuthorize("hasAuthority('PERM_CREATE_USER')")
    public String addForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        model.addAttribute("allRoles", roleRepo.findAll());
        return "users/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_USER')")
    public String add(@Valid @ModelAttribute("userDto") UserDto dto,
                      BindingResult result, Model model,
                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("allRoles", roleRepo.findAll());
            return "users/add";
        }
        try {
            userService.create(dto);
            redirectAttributes.addFlashAttribute("flashSuccess", "User created successfully");
            return "redirect:/users";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/users/add";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_USER')")
    public String editForm(@PathVariable Long id, Model model) {
        var user = userService.findById(id);
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setEnabled(user.isEnabled());
        if (user.getRoles() != null) {
            user.getRoles().forEach(r -> dto.getRoleIds().add(r.getId()));
        }
        model.addAttribute("userDto", dto);
        model.addAttribute("allRoles", roleRepo.findAll());
        return "users/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_USER')")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("userDto") UserDto dto,
                       BindingResult result, Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("allRoles", roleRepo.findAll());
            return "users/edit";
        }
        try {
            userService.update(id, dto);
            redirectAttributes.addFlashAttribute("flashSuccess", "User updated successfully");
            return "redirect:/users";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/users/edit/" + id;
        }
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAuthority('PERM_VIEW_USER')")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        return "users/view";
    }

    @GetMapping("/toggle/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_USER')")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var user = userService.toggleStatus(id);
        redirectAttributes.addFlashAttribute("flashSuccess",
                user.isEnabled() ? "User activated" : "User deactivated");
        return "redirect:/users";
    }

    @PostMapping("/reset-password/{id}")
    @PreAuthorize("hasAuthority('PERM_RESET_PASSWORD')")
    public String resetPassword(@PathVariable Long id,
                                @RequestParam String newPassword,
                                RedirectAttributes redirectAttributes) {
        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("flashError", "Password must be at least 6 characters");
            return "redirect:/users";
        }
        userService.resetPassword(id, newPassword);
        redirectAttributes.addFlashAttribute("flashSuccess", "Password has been reset successfully");
        return "redirect:/users";
    }
}
