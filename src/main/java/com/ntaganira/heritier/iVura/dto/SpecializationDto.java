package com.ntaganira.heritier.iVura.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.dto
 * - File      : SpecializationDto.java
 * - Date      : 2026. 08. 07.
 * - User      : Hntaganira
 * - Desc      : Specialization DTO
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class SpecializationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "Specialization name is required")
    private String name;

    private String description;
}
