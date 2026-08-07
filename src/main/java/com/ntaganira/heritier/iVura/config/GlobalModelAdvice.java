package com.ntaganira.heritier.iVura.config;

import com.ntaganira.heritier.iVura.entity.User;
import com.ntaganira.heritier.iVura.repository.UserRepository;
import com.ntaganira.heritier.iVura.service.FileStorageService;
import com.ntaganira.heritier.iVura.service.NotificationService;
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
    private final NotificationService notificationService;

    public GlobalModelAdvice(UserRepository userRepo, FileStorageService fileStorageService,
                             NotificationService notificationService) {
        this.userRepo = userRepo;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
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

    @ModelAttribute("unreadNotificationCount")
    public long unreadNotificationCount() {
        Long userId = notificationService.currentUserId();
        return notificationService.unreadCount(userId);
    }

    @ModelAttribute("recentNotifications")
    public java.util.List<com.ntaganira.heritier.iVura.entity.Notification> recentNotifications() {
        Long userId = notificationService.currentUserId();
        return userId == null ? java.util.List.of() : notificationService.recent(userId, 5);
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || (auth.getPrincipal() instanceof String)) {
            return null;
        }
        return userRepo.findByUsername(auth.getName()).orElse(null);
    }
}
