package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.WardRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : WardRoomRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Ward Room Repository
 * </pre>
 */
public interface WardRoomRepository extends JpaRepository<WardRoom, Long> {
    List<WardRoom> findByIsActiveTrueOrderByRoomNumberAsc();
}
