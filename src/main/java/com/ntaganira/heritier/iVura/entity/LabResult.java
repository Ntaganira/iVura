package com.ntaganira.heritier.iVura.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.entity
 * - File      : LabResult.java
 * - Desc      : Laboratory Result Entity
 * </pre>
 */
@Entity
@Table(name = "lab_results")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "test_name", nullable = false, length = 150)
    private String testName;

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(length = 30)
    private String unit;

    @Column(name = "normal_range", length = 100)
    private String normalRange;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false, length = 10)
    private String source = "MANUAL";

    @Column(name = "accession_number", length = 50)
    private String accessionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private AnalyzerDevice device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_message_id")
    private InstrumentMessage instrumentMessage;

    @Column(length = 10)
    private String flag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private User publishedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "override_reason", columnDefinition = "TEXT")
    private String overrideReason;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (performedAt == null) {
            performedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if ("COMPLETED".equals(status) && performedAt == null) {
            performedAt = LocalDateTime.now();
        }
    }
}
