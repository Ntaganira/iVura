package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.DoctorShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : DoctorShiftRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Doctor Shift Repository
 * </pre>
 */
public interface DoctorShiftRepository extends JpaRepository<DoctorShift, Long> {
    List<DoctorShift> findByDoctorId(Long doctorId);
    Optional<DoctorShift> findByDoctorIdAndDayOfWeek(Long doctorId, Integer dayOfWeek);
    List<DoctorShift> findByDayOfWeek(Integer dayOfWeek);
}
