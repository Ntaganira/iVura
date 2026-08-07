package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.AppointmentDto;
import com.ntaganira.heritier.iVura.entity.Appointment;
import com.ntaganira.heritier.iVura.entity.Doctor;
import com.ntaganira.heritier.iVura.enums.AppointmentStatus;
import com.ntaganira.heritier.iVura.repository.AppointmentRepository;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import com.ntaganira.heritier.iVura.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

    public static final int SLOT_MINUTES = 30;

    private final AppointmentRepository appointmentRepo;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final ServiceRepository serviceRepo;
    private final NotificationService notificationService;

    public AppointmentService(AppointmentRepository appointmentRepo,
                               PatientRepository patientRepo,
                               DoctorRepository doctorRepo,
                               ServiceRepository serviceRepo,
                               NotificationService notificationService) {
        this.appointmentRepo = appointmentRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.serviceRepo = serviceRepo;
        this.notificationService = notificationService;
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

    @Transactional
    public Appointment create(AppointmentDto dto) {
        Doctor doctor = doctorRepo.findById(dto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        validateSlot(doctor, dto.getAppointmentDate(), dto.getAppointmentTime(), null);
        Appointment appointment = Appointment.builder()
            .patient(patientRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found")))
            .doctor(doctor)
            .service(dto.getServiceId() != null
                ? serviceRepo.findById(dto.getServiceId()).orElse(null)
                : null)
            .appointmentDate(dto.getAppointmentDate())
            .appointmentTime(dto.getAppointmentTime())
            .status(AppointmentStatus.SCHEDULED)
            .reason(dto.getReason())
            .notes(dto.getNotes())
            .build();
        Appointment saved = appointmentRepo.save(appointment);
        notificationService.notifyAll(
                "New appointment scheduled",
                appointment.getPatient().getFullName() + " scheduled with "
                        + appointment.getDoctor().getFullName() + " on " + appointment.getAppointmentDate()
                        + " at " + appointment.getAppointmentTime(),
                NotificationService.TYPE_APPOINTMENT);
        return saved;
    }

    public Appointment updateStatus(Long id, String status) {
        Appointment appointment = findById(id);
        appointment.setStatus(AppointmentStatus.valueOf(status));
        return appointmentRepo.save(appointment);
    }

    @Transactional
    public Appointment reschedule(Long id, LocalDate date, LocalTime time) {
        Appointment appointment = findById(id);
        validateSlot(appointment.getDoctor(), date, time, id);
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        return appointmentRepo.save(appointment);
    }

    public void delete(Long id) {
        appointmentRepo.deleteById(id);
    }

    /**
     * Automated conflict-checker: verifies the requested slot falls inside the
     * doctor's availability window and does not overlap another appointment
     * (30-minute slots; cancelled and no-show appointments are ignored).
     */
    public void validateSlot(Doctor doctor, LocalDate date, LocalTime time, Long excludeId) {
        if (doctor == null) {
            throw new IllegalArgumentException("A doctor must be selected");
        }
        if (time == null) {
            throw new IllegalArgumentException("Appointment time is required");
        }
        LocalTime slotEnd = time.plusMinutes(SLOT_MINUTES);
        if (doctor.getAvailableFrom() != null && doctor.getAvailableTo() != null) {
            if (time.isBefore(doctor.getAvailableFrom()) || slotEnd.isAfter(doctor.getAvailableTo())) {
                throw new IllegalArgumentException(
                        "Doctor " + doctor.getFullName() + " is not available at " + time
                                + " (availability " + doctor.getAvailableFrom() + " - " + doctor.getAvailableTo() + ")");
            }
        }
        List<Appointment> existing = appointmentRepo.findByDoctorIdAndAppointmentDate(doctor.getId(), date);
        for (Appointment a : existing) {
            if (excludeId != null && a.getId().equals(excludeId)) {
                continue;
            }
            AppointmentStatus status = a.getStatus();
            if (status == AppointmentStatus.CANCELLED || status == AppointmentStatus.NO_SHOW) {
                continue;
            }
            LocalTime otherEnd = a.getAppointmentTime().plusMinutes(SLOT_MINUTES);
            if (time.isBefore(otherEnd) && a.getAppointmentTime().isBefore(slotEnd)) {
                throw new IllegalArgumentException(
                        "Time slot conflict: Dr. " + doctor.getFullName() + " already has an appointment at "
                                + a.getAppointmentTime() + " on " + date + " for " + a.getPatient().getFullName());
            }
        }
    }
}
