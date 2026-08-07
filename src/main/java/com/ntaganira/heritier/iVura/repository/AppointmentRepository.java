package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Appointment;
import com.ntaganira.heritier.iVura.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByAppointmentDate(LocalDate date);
    List<Appointment> findByAppointmentDateBetween(LocalDate start, LocalDate end);
    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByStatus(AppointmentStatus status);
    long countByAppointmentDate(LocalDate date);
    long countByStatus(AppointmentStatus status);
    long countByAppointmentDateAndStatus(LocalDate date, AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate BETWEEN :from AND :to")
    long countBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT CONCAT(a.doctor.firstName, ' ', a.doctor.lastName), COUNT(a) FROM Appointment a " +
            "WHERE a.appointmentDate BETWEEN :from AND :to GROUP BY a.doctor.firstName, a.doctor.lastName ORDER BY COUNT(a) DESC")
    List<Object[]> countByDoctorBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
