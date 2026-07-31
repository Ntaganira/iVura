package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.entity.ActivityLog;
import com.ntaganira.heritier.iVura.entity.User;
import com.ntaganira.heritier.iVura.repository.UserRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Controller
@RequestMapping("/activity")
public class ActivityLogController {

    private static final int PAGE_SIZE = 10;

    private final ActivityLogService activityLogService;
    private final UserRepository userRepo;

    public ActivityLogController(ActivityLogService activityLogService, UserRepository userRepo) {
        this.activityLogService = activityLogService;
        this.userRepo = userRepo;
    }

    @GetMapping("/me")
    public String myActivity(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(required = false) String module,
                             @RequestParam(required = false) String action,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                             @RequestParam(required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                             Model model) {
        User user = currentUser();
        if (user == null) {
            return "redirect:/login";
        }
        Page<ActivityLog> logs = activityLogService.findPage(user.getId(), module, action,
                status, from, to, page, PAGE_SIZE);
        populate(model, logs, module, action, status, from, to);
        return "activity/my";
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_ACTIVITY_LOG')")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) Long userId,
                       @RequestParam(required = false) String module,
                       @RequestParam(required = false) String action,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       Model model) {
        Page<ActivityLog> logs = activityLogService.findPage(userId, module, action,
                status, from, to, page, PAGE_SIZE);
        model.addAttribute("users", userRepo.findAll(Sort.by(Sort.Direction.ASC, "fullName")));
        model.addAttribute("selectedUserId", userId);
        populate(model, logs, module, action, status, from, to);
        return "activity/list";
    }

    private void populate(Model model, Page<ActivityLog> logs, String module, String action,
                          String status, LocalDate from, LocalDate to) {
        model.addAttribute("logs", logs);
        model.addAttribute("modules", activityLogService.findModules());
        model.addAttribute("actions", activityLogService.findActions());
        model.addAttribute("module", module);
        model.addAttribute("action", action);
        model.addAttribute("status", status);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("paginationQuery", buildQuery(null, module, action, status, from, to));
    }

    private String buildQuery(Long userId, String module, String action,
                              String status, LocalDate from, LocalDate to) {
        StringBuilder q = new StringBuilder();
        if (userId != null) {
            q.append("userId=").append(userId);
        }
        if (StringUtils.hasText(module)) {
            if (q.length() > 0) q.append('&');
            q.append("module=").append(URLEncoder.encode(module.trim(), StandardCharsets.UTF_8));
        }
        if (StringUtils.hasText(action)) {
            if (q.length() > 0) q.append('&');
            q.append("action=").append(URLEncoder.encode(action.trim(), StandardCharsets.UTF_8));
        }
        if (StringUtils.hasText(status)) {
            if (q.length() > 0) q.append('&');
            q.append("status=").append(status.trim());
        }
        if (from != null) {
            if (q.length() > 0) q.append('&');
            q.append("from=").append(from);
        }
        if (to != null) {
            if (q.length() > 0) q.append('&');
            q.append("to=").append(to);
        }
        return q.toString();
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return userRepo.findByUsername(auth.getName()).orElse(null);
    }
}
