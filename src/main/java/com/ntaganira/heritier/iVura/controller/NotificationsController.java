package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.entity.Notification;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/notifications")
public class NotificationsController {

    private static final int PAGE_SIZE = 10;

    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public NotificationsController(NotificationService notificationService,
                                   ActivityLogService activityLogService) {
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_NOTIFICATION')")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String status,
                       Model model) {
        Long userId = notificationService.currentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        boolean unreadOnly = "unread".equalsIgnoreCase(status);
        Page<Notification> notifications = notificationService.findPage(userId, unreadOnly, page, PAGE_SIZE);
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadOnly", unreadOnly);
        model.addAttribute("unreadCount", notificationService.unreadCount(userId));
        model.addAttribute("paginationQuery", buildQuery(unreadOnly));
        return "notifications/list";
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasAuthority('PERM_MARK_NOTIFICATION_READ')")
    public String markRead(@PathVariable Long id) {
        Long userId = notificationService.currentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        notificationService.markRead(id, userId);
        activityLogService.record("Notifications", "READ_NOTIFICATION",
                "Marked notification #" + id + " as read", ActivityStatus.SUCCESS);
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    @PreAuthorize("hasAuthority('PERM_MARK_NOTIFICATION_READ')")
    public String markAllRead(RedirectAttributes redirectAttributes) {
        Long userId = notificationService.currentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        int count = notificationService.markAllRead(userId);
        activityLogService.record("Notifications", "READ_NOTIFICATIONS",
                "Marked " + count + " notification(s) as read", ActivityStatus.SUCCESS);
        redirectAttributes.addFlashAttribute("flashSuccess", count + " notification(s) marked as read");
        return "redirect:/notifications";
    }

    @PostMapping("/send")
    @PreAuthorize("hasAuthority('PERM_CREATE_NOTIFICATION')")
    public String send(@RequestParam String title, @RequestParam(required = false) String message,
                       RedirectAttributes redirectAttributes) {
        if (!StringUtils.hasText(title)) {
            redirectAttributes.addFlashAttribute("flashError", "Notification title is required");
            return "redirect:/notifications";
        }
        notificationService.notifyAll(title.trim(), message, NotificationService.TYPE_SYSTEM, null);
        activityLogService.record("Notifications", "BROADCAST_NOTIFICATION",
                "Broadcast notification: " + title.trim(), ActivityStatus.SUCCESS);
        redirectAttributes.addFlashAttribute("flashSuccess", "Notification sent to all users");
        return "redirect:/notifications";
    }

    private String buildQuery(boolean unreadOnly) {
        return unreadOnly ? "status=unread" : "";
    }
}
