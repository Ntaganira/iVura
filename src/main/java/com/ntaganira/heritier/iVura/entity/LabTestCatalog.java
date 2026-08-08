package com.ntaganira.heritier.iVura.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.entity
 * - File      : LabTestCatalog.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Laboratory Test Catalog Entity
 * </pre>
 */
@Entity
@Table(name = "lab_test_catalog")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabTestCatalog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String category;

    @Column(name = "specimen_type", length = 50)
    private String specimenType;

    @Column(length = 30)
    private String unit;

    @Column(name = "normal_range", length = 100)
    private String normalRange;

    @Column(name = "ref_low", precision = 14, scale = 4)
    private BigDecimal refLow;

    @Column(name = "ref_high", precision = 14, scale = 4)
    private BigDecimal refHigh;

    @Column(name = "critical_low", precision = 14, scale = 4)
    private BigDecimal criticalLow;

    @Column(name = "critical_high", precision = 14, scale = 4)
    private BigDecimal criticalHigh;

    @Column(name = "delta_threshold", precision = 14, scale = 4)
    private BigDecimal deltaThreshold;

    @Column(name = "auto_verify_eligible")
    private Boolean autoVerifyEligible = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (autoVerifyEligible == null) {
            autoVerifyEligible = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
