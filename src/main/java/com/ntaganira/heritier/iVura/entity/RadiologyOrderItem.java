package com.ntaganira.heritier.iVura.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.entity
 * - File      : RadiologyOrderItem.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Radiology Order Item Entity
 * </pre>
 */
@Entity
@Table(name = "radiology_order_items")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RadiologyOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_ORDERED = "ORDERED";
    public static final String STATUS_IMAGED = "IMAGED";
    public static final String STATUS_REPORTED = "REPORTED";
    public static final String STATUS_VERIFIED = "VERIFIED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private RadiologyOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    private RadiologyExam exam;

    @Column(name = "exam_name", nullable = false, length = 150)
    private String examName;

    @Builder.Default
    @Column(nullable = false, length = 30)
    private String status = STATUS_ORDERED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
