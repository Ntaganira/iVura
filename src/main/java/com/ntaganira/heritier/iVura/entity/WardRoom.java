package com.ntaganira.heritier.iVura.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.entity
 * - File      : WardRoom.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Ward Room Entity
 * </pre>
 */
@Entity
@Table(name = "ward_rooms")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WardRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false, unique = true, length = 20)
    private String roomNumber;

    @Column(name = "ward_name", length = 50)
    private String wardName;

    @Column(name = "room_type", nullable = false, length = 30)
    private String roomType = "WARD";

    @Column(name = "price_per_night", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerNight = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer capacity = 1;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
