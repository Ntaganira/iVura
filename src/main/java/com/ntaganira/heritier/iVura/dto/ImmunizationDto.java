package com.ntaganira.heritier.iVura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.dto
 * - File      : ImmunizationDto.java
 * - Desc      : Immunization (vaccination log) DTO
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class ImmunizationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "Patient is required")
    private Long patientId;

    @NotBlank(message = "Vaccine is required")
    private String vaccine;

    private Integer doseNumber = 1;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate administeredDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate nextDueDate;

    private Long administeredById;

    private String batchNumber;

    private String notes;
}
