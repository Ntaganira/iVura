package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Page<Notification> findByUserIdAndReadFalse(Long userId, Pageable pageable);

    List<Notification> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :now " +
            "WHERE n.userId = :userId AND n.read = false")
    int markAllRead(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :now " +
            "WHERE n.id = :id AND n.userId = :userId")
    int markRead(@Param("id") Long id, @Param("userId") Long userId, @Param("now") LocalDateTime now);
}
