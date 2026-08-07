package com.ntaganira.heritier.iVura.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.dto
 * - File      : MedicalRecordDto.java
 * - Desc      : Medical Record DTO
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "Patient is required")
    private Long patientId;

    private Long doctorId;

    private Long appointmentId;

    private String diagnosis;

    private String prescription;

    private String notes;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate recordDate;

    private String patientName;
    private String doctorName;
}
