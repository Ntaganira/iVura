package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : AttendanceRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Attendance Repository
 * </pre>
 */
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByDoctorIdAndAttendanceDate(Long doctorId, LocalDate date);
    List<Attendance> findByAttendanceDateOrderByDoctorFirstNameAsc(LocalDate date);
    List<Attendance> findByDoctorIdOrderByAttendanceDateDesc(Long doctorId);
    long countByAttendanceDateAndStatus(LocalDate date, String status);
}
