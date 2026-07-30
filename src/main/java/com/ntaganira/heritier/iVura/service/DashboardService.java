package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.enums.AppointmentStatus;
import com.ntaganira.heritier.iVura.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class DashboardService {

    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final AppointmentRepository appointmentRepo;
    private final DepartmentRepository departmentRepo;

    public DashboardService(PatientRepository patientRepo, DoctorRepository doctorRepo,
                            AppointmentRepository appointmentRepo, DepartmentRepository departmentRepo) {
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.appointmentRepo = appointmentRepo;
        this.departmentRepo = departmentRepo;
    }

    public long getTotalPatients() {
        return patientRepo.count();
    }

    public long getTotalDoctors() {
        return doctorRepo.count();
    }

    public long getTodayAppointments() {
        return appointmentRepo.countByAppointmentDate(LocalDate.now());
    }

    public long getTotalDepartments() {
        return departmentRepo.count();
    }

    public long getCompletedAppointmentsToday() {
        return appointmentRepo.countByAppointmentDateAndStatus(LocalDate.now(), AppointmentStatus.COMPLETED);
    }

    public long getPendingAppointments() {
        return appointmentRepo.countByStatus(AppointmentStatus.SCHEDULED);
    }

    public List<Object[]> getWeeklyAppointments() {
        List<Object[]> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long count = appointmentRepo.countByAppointmentDate(date);
            result.add(new Object[]{date.toString(), count});
        }
        return result;
    }
}
