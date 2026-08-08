package com.ntaganira.heritier.iVura.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.entity
 * - File      : LabOrderItem.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Laboratory Order Item Entity
 * </pre>
 */
@Entity
@Table(name = "lab_order_items")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_ORDERED = "ORDERED";
    public static final String STATUS_RESULT_RECEIVED = "RESULT_RECEIVED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private LabOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_id")
    private LabTestCatalog catalog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id")
    private LabResult result;

    @Column(name = "test_name", nullable = false, length = 150)
    private String testName;

    @Column(nullable = false, length = 30)
    private String status = STATUS_ORDERED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
