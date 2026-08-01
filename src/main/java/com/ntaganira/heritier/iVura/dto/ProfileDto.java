package com.ntaganira.heritier.iVura.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.dto
 * - File      : ProfileDto.java
 * - Date      : 2026. 08. 01.
 * - User      : Hntaganira
 * - Desc      : Self-service profile DTO (name, email, phone, optional password change)
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private String currentPassword;

    private String newPassword;

    private String confirmPassword;

    private String photoUrl;
}
