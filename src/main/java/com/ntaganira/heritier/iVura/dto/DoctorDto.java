package com.ntaganira.heritier.iVura.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.dto
 * - File      : DoctorDto.java
 * - Date      : 2026. 07. 30.
 * - User      : Hntaganira
 * - Desc      : Doctor DTO
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private String phone;
    private List<Long> specializationIds = new ArrayList<>();
    private List<Long> serviceIds = new ArrayList<>();
    private String licenseNumber;
    private Long departmentId;
    private String qualification;
    private Integer experienceYears;
    private LocalTime availableFrom;
    private LocalTime availableTo;
}
