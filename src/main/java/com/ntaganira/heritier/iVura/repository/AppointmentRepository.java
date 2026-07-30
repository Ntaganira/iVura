package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Appointment;
import com.ntaganira.heritier.iVura.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByAppointmentDate(LocalDate date);
    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByStatus(AppointmentStatus status);
    long countByAppointmentDate(LocalDate date);
    long countByStatus(AppointmentStatus status);
    long countByAppointmentDateAndStatus(LocalDate date, AppointmentStatus status);
}
