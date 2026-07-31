package com.ntaganira.heritier.iVura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.dto
 * - File      : RoleDto.java
 * - Date      : 2026. 07. 31.
 * - User      : Hntaganira
 * - Desc      : Role DTO
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "Role name is required")
    private String name;

    @NotBlank(message = "Role code is required")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Code must be uppercase letters, numbers and underscores")
    private String code;

    private String description;

    private boolean enabled = true;

    private Set<Long> permissionIds = new HashSet<>();

    private Set<Long> pageIds = new HashSet<>();
}
