package com.ntaganira.heritier.iVura.config;

import com.ntaganira.heritier.iVura.entity.User;
import com.ntaganira.heritier.iVura.repository.UserRepository;
import com.ntaganira.heritier.iVura.service.FileStorageService;
import com.ntaganira.heritier.iVura.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final UserRepository userRepo;
    private final FileStorageService fileStorageService;

    public GlobalModelAdvice(UserRepository userRepo, FileStorageService fileStorageService) {
        this.userRepo = userRepo;
        this.fileStorageService = fileStorageService;
    }

    @ModelAttribute("requestURI")
    public String requestURI(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("profileInitials")
    public String profileInitials() {
        User user = currentUser();
        return user == null ? "" : UserService.initials(user.getFullName());
    }

    @ModelAttribute("profileFullName")
    public String profileFullName() {
        User user = currentUser();
        return user == null ? "" : user.getFullName();
    }

    @ModelAttribute("profilePhotoUrl")
    public String profilePhotoUrl() {
        User user = currentUser();
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
        if (auth == null || !auth.isAuthenticated() || (auth.getPrincipal() instanceof String)) {
            return null;
        }
        return userRepo.findByUsername(auth.getName()).orElse(null);
    }
}
