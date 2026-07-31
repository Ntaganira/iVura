package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.entity.ActivityLog;
import com.ntaganira.heritier.iVura.entity.User;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.ActivityLogRepository;
import com.ntaganira.heritier.iVura.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
public class ActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogService.class);
    private static final Set<String> STATUSES = Set.of("SUCCESS", "FAILED");

    private final ActivityLogRepository logRepo;
    private final UserRepository userRepo;

    public ActivityLogService(ActivityLogRepository logRepo, UserRepository userRepo) {
        this.logRepo = logRepo;
        this.userRepo = userRepo;
    }

    /**
     * Records an activity for the given username (resolving the user id).
     */
    public void record(String username, String module, String action,
                       String description, ActivityStatus status, String ipAddress) {
        try {
            Long userId = null;
            if (StringUtils.hasText(username)) {
                userId = userRepo.findByUsername(username).map(User::getId).orElse(null);
            }
            logRepo.save(ActivityLog.builder()
                    .userId(userId)
                    .username(username)
                    .module(module)
                    .action(action)
                    .description(description)
                    .status(status)
                    .ipAddress(ipAddress)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to record activity log: {}", e.getMessage());
        }
    }

    /**
     * Records an activity for the currently authenticated user.
     */
    public void record(String module, String action, String description, ActivityStatus status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            username = auth.getName();
        }
        record(username, module, action, description, status, resolveClientIp());
    }

    public Page<ActivityLog> findPage(Long userId, String module, String action,
                                      String status, LocalDate from, LocalDate to,
                                      int page, int size) {
        Specification<ActivityLog> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (userId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("userId"), userId));
            }
            if (StringUtils.hasText(module)) {
                predicate = cb.and(predicate, cb.equal(root.get("module"), module.trim()));
            }
            if (StringUtils.hasText(action)) {
                predicate = cb.and(predicate, cb.equal(root.get("action"), action.trim()));
            }
            if (StringUtils.hasText(status) && STATUSES.contains(status.trim().toUpperCase())) {
                predicate = cb.and(predicate, cb.equal(root.get("status"),
                        ActivityStatus.valueOf(status.trim().toUpperCase())));
            }
            if (from != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
            }
            if (to != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("createdAt"), to.atTime(LocalTime.MAX)));
            }
            return predicate;
        };
        return logRepo.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public List<String> findModules() {
        return logRepo.findDistinctModules();
    }

    public List<String> findActions() {
        return logRepo.findDistinctActions();
    }

    private String resolveClientIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
