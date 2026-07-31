package com.ntaganira.heritier.iVura.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.dto
 * - File      : AppointmentDto.java
 * - Date      : 2026. 07. 30.
 * - User      : Hntaganira
 * - Desc      : Appointment DTO
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "Patient is required")
    private Long patientId;

    @NotNull(message = "Doctor is required")
    private Long doctorId;

    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate appointmentDate;

    @NotNull(message = "Appointment time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime appointmentTime;

    private String status;
    private String reason;
    private String notes;

    private String patientName;
    private String doctorName;
}
