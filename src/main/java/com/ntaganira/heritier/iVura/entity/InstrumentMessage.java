package com.ntaganira.heritier.iVura.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.entity
 * - File      : InstrumentMessage.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Instrument Message (raw inbound/outbound payload) Entity
 * </pre>
 */
@Entity
@Table(name = "instrument_messages")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String DIR_INBOUND = "INBOUND";
    public static final String DIR_OUTBOUND = "OUTBOUND";

    public static final String STATUS_RECEIVED = "RECEIVED";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_INCOMPLETE = "INCOMPLETE";
    public static final String STATUS_UNMATCHED = "UNMATCHED";
    public static final String STATUS_DUPLICATE = "DUPLICATE";
    public static final String STATUS_ERROR = "ERROR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, unique = true, length = 100)
    private String messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private AnalyzerDevice device;

    @Column(nullable = false, length = 10)
    private String direction = DIR_INBOUND;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false, length = 30)
    private String status = STATUS_RECEIVED;

    @Column(name = "accession_number", length = 50)
    private String accessionNumber;

    @Column(name = "patient_ref", length = 100)
    private String patientRef;

    @Column(name = "error_code", length = 30)
    private String errorCode;

    @Column(name = "error_detail", columnDefinition = "TEXT")
    private String errorDetail;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
