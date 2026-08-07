package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.entity.Notification;
import com.ntaganira.heritier.iVura.entity.User;
import com.ntaganira.heritier.iVura.repository.NotificationRepository;
import com.ntaganira.heritier.iVura.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public static final String TYPE_INFO = "INFO";
    public static final String TYPE_SUCCESS = "SUCCESS";
    public static final String TYPE_WARNING = "WARNING";
    public static final String TYPE_PAYMENT = "PAYMENT";
    public static final String TYPE_BILL = "BILL";
    public static final String TYPE_APPOINTMENT = "APPOINTMENT";
    public static final String TYPE_SYSTEM = "SYSTEM";

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;

    public NotificationService(NotificationRepository notificationRepo, UserRepository userRepo) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public void notifyUser(Long userId, String title, String message, String type) {
        try {
            if (userId == null) {
                return;
            }
            notificationRepo.save(Notification.builder()
                    .userId(userId)
                    .title(title)
                    .message(message)
                    .type(type != null ? type : TYPE_INFO)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to notify user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Broadcasts a notification to every enabled user, excluding the actor.
     */
    @Transactional
    public void notifyAll(String title, String message, String type) {
        notifyAll(title, message, type, currentUserId());
    }

    /**
     * Broadcasts a notification to every enabled user, optionally excluding the actor.
     */
    @Transactional
    public void notifyAll(String title, String message, String type, Long excludeUserId) {
        List<User> users = userRepo.findByEnabledTrue();
        for (User user : users) {
            if (excludeUserId != null && excludeUserId.equals(user.getId())) {
                continue;
            }
            notifyUser(user.getId(), title, message, type);
        }
    }

    public Page<Notification> findPage(Long userId, boolean unreadOnly, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return unreadOnly
                ? notificationRepo.findByUserIdAndReadFalse(userId, pageable)
                : notificationRepo.findByUserId(userId, pageable);
    }

    public List<Notification> recent(Long userId, int limit) {
        return notificationRepo.findTop5ByUserIdOrderByCreatedAtDesc(userId);
    }

    public long unreadCount(Long userId) {
        return userId == null ? 0 : notificationRepo.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public boolean markRead(Long id, Long userId) {
        return notificationRepo.markRead(id, userId, LocalDateTime.now()) > 0;
    }

    @Transactional
    public int markAllRead(Long userId) {
        return notificationRepo.markAllRead(userId, LocalDateTime.now());
    }

    public Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || (auth.getPrincipal() instanceof String)) {
            return null;
        }
        return userRepo.findByUsername(auth.getName()).map(User::getId).orElse(null);
    }
}
