package com.ntaganira.heritier.iVura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serializable;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.dto
 * - File      : PermissionDto.java
 * - Date      : 2026. 07. 31.
 * - User      : Hntaganira
 * - Desc      : Permission DTO
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "Permission code is required")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Code must be uppercase letters, numbers and underscores")
    private String code;

    @NotBlank(message = "Permission name is required")
    private String name;

    @NotBlank(message = "Module is required")
    private String module;

    @NotBlank(message = "Action is required")
    private String action;

    private String description;

    private boolean enabled = true;
}
