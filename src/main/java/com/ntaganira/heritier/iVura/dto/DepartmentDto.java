package com.ntaganira.heritier.iVura.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.dto
 * - File      : DepartmentDto.java
 * - Date      : 2026. 08. 01.
 * - User      : Hntaganira
 * - Desc      : Department DTO
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "Department name is required")
    private String name;

    private String description;
    private String phone;
    private String location;
}
