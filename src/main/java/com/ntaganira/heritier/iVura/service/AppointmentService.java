package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.AppointmentDto;
import com.ntaganira.heritier.iVura.entity.Appointment;
import com.ntaganira.heritier.iVura.enums.AppointmentStatus;
import com.ntaganira.heritier.iVura.repository.AppointmentRepository;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;

    public AppointmentService(AppointmentRepository appointmentRepo,
                               PatientRepository patientRepo,
                               DoctorRepository doctorRepo) {
        this.appointmentRepo = appointmentRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
    }

    public List<Appointment> findAll() {
        return appointmentRepo.findAll();
    }

    public List<Appointment> findBetween(LocalDate start, LocalDate end) {
        return appointmentRepo.findByAppointmentDateBetween(start, end);
    }

    public Appointment findById(Long id) {
        return appointmentRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
    }

    public Appointment create(AppointmentDto dto) {
        Appointment appointment = Appointment.builder()
            .patient(patientRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found")))
            .doctor(doctorRepo.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found")))
            .appointmentDate(dto.getAppointmentDate())
            .appointmentTime(dto.getAppointmentTime())
            .status(AppointmentStatus.SCHEDULED)
            .reason(dto.getReason())
            .notes(dto.getNotes())
            .build();
        return appointmentRepo.save(appointment);
    }

    public Appointment updateStatus(Long id, String status) {
        Appointment appointment = findById(id);
        appointment.setStatus(AppointmentStatus.valueOf(status));
        return appointmentRepo.save(appointment);
    }

    public Appointment reschedule(Long id, LocalDate date, LocalTime time) {
        Appointment appointment = findById(id);
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        return appointmentRepo.save(appointment);
    }

    public void delete(Long id) {
        appointmentRepo.deleteById(id);
    }
}
