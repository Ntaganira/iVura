package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.ProfileDto;
import com.ntaganira.heritier.iVura.entity.User;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.UserRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.FileStorageService;
import com.ntaganira.heritier.iVura.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : ProfileController.java
 * - Date      : 2026. 08. 01.
 * - User      : Hntaganira
 * - Desc      : Self-service "Update My Profile" page
 * </pre>
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final String AVATAR_PREFIX = "avatars";

    private final UserService userService;
    private final UserRepository userRepo;
    private final FileStorageService fileStorageService;
    private final ActivityLogService activityLogService;

    public ProfileController(UserService userService, UserRepository userRepo,
                             FileStorageService fileStorageService,
                             ActivityLogService activityLogService) {
        this.userService = userService;
        this.userRepo = userRepo;
        this.fileStorageService = fileStorageService;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public String show(Model model) {
        User user = currentUser();
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("profileDto", toDto(user));
        model.addAttribute("profileInitials", UserService.initials(user.getFullName()));
        model.addAttribute("profilePhotoUrl", photoUrl(user));
        return "profile/edit";
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public String update(@Valid @ModelAttribute("profileDto") ProfileDto dto,
                         BindingResult result,
                         @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
                         Model model, RedirectAttributes redirectAttributes) {
        User user = currentUser();
        if (user == null) {
            return "redirect:/login";
        }
        if (result.hasErrors()) {
            model.addAttribute("formErrors", result.getFieldErrors());
            model.addAttribute("profileInitials", UserService.initials(user.getFullName()));
            model.addAttribute("profilePhotoUrl", photoUrl(user));
            return "profile/edit";
        }
        try {
            if (photoFile != null && !photoFile.isEmpty()) {
                validateImage(photoFile);
            }
            userService.updateProfile(user.getId(), dto);
            if (photoFile != null && !photoFile.isEmpty()) {
                String objectName = fileStorageService.upload(photoFile, AVATAR_PREFIX);
                if (StringUtils.hasText(user.getPhotoUrl())) {
                    try {
                        fileStorageService.delete(user.getPhotoUrl());
                    } catch (RuntimeException ignored) {
                        // old photo no longer exists - ignore
                    }
                }
                user.setPhotoUrl(objectName);
                userRepo.save(user);
            }
            activityLogService.record("User Management", "UPDATE_PROFILE",
                    "Updated own profile for " + user.getUsername(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Profile updated successfully");
            return "redirect:/profile";
        } catch (RuntimeException e) {
            activityLogService.record("User Management", "UPDATE_PROFILE",
                    "Failed to update own profile for " + user.getUsername(), ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/profile";
        }
    }

    private void validateImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Please select an image file");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new RuntimeException("Image must be smaller than 5MB");
        }
    }

    private ProfileDto toDto(User user) {
        ProfileDto dto = new ProfileDto();
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setPhotoUrl(user.getPhotoUrl());
        return dto;
    }

    private String photoUrl(User user) {
        if (user == null || !StringUtils.hasText(user.getPhotoUrl())) {
            return null;
        }
        try {
            return fileStorageService.getUrl(user.getPhotoUrl());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return userRepo.findByUsername(auth.getName()).orElse(null);
    }
}
