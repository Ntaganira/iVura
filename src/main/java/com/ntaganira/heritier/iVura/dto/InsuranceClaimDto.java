package com.ntaganira.heritier.iVura.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.dto
 * - File      : InsuranceClaimDto.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Insurance Claim DTO
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceClaimDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "Patient is required")
    private Long patientId;

    private Long billingId;

    @NotBlank(message = "Insurance provider is required")
    private String provider;

    private String policyNumber;

    @NotNull(message = "Claim amount is required")
    @DecimalMin(value = "0.0", message = "Amount cannot be negative")
    private BigDecimal amount;

    private String status = "SUBMITTED";

    private String remarks;
}
