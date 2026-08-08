package com.ntaganira.heritier.iVura.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.entity
 * - File      : ResultSignoff.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Lab Result Sign-off (verify/publish/override/reject) Entity
 * </pre>
 */
@Entity
@Table(name = "result_signoffs")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultSignoff implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ACTION_VERIFY = "VERIFY";
    public static final String ACTION_PUBLISH = "PUBLISH";
    public static final String ACTION_OVERRIDE = "OVERRIDE";
    public static final String ACTION_REJECT = "REJECT";
    public static final String ACTION_HOLD = "HOLD";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_result_id", nullable = false)
    private LabResult labResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signoff_user_id")
    private User signoffUser;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
