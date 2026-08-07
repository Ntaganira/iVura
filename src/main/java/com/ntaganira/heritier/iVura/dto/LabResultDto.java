package com.ntaganira.heritier.iVura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.dto
 * - File      : LabResultDto.java
 * - Desc      : Laboratory Result DTO
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class LabResultDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "Patient is required")
    private Long patientId;

    private Long doctorId;

    @NotBlank(message = "Test name is required")
    private String testName;

    private String category;

    private String result;

    private String unit;

    private String normalRange;

    private String status = "PENDING";

    private String notes;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime performedAt;

    private String patientName;
}
