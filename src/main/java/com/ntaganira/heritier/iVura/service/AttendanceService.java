package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.entity.Attendance;
import com.ntaganira.heritier.iVura.entity.Doctor;
import com.ntaganira.heritier.iVura.entity.DoctorShift;
import com.ntaganira.heritier.iVura.repository.AttendanceRepository;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.DoctorShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.service
 * - File      : AttendanceService.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Doctor attendance and shift service
 * </pre>
 */
@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepo;
    private final DoctorRepository doctorRepo;
    private final DoctorShiftRepository shiftRepo;

    public AttendanceService(AttendanceRepository attendanceRepo,
                             DoctorRepository doctorRepo,
                             DoctorShiftRepository shiftRepo) {
        this.attendanceRepo = attendanceRepo;
        this.doctorRepo = doctorRepo;
        this.shiftRepo = shiftRepo;
    }

    public List<Attendance> findByDate(LocalDate date) {
        return attendanceRepo.findByAttendanceDateOrderByDoctorFirstNameAsc(date);
    }

    public Map<Long, Attendance> attendanceByDoctor(LocalDate date) {
        return findByDate(date).stream()
                .collect(Collectors.toMap(a -> a.getDoctor().getId(), Function.identity(), (a, b) -> a));
    }

    public DoctorShift shiftFor(Doctor doctor, LocalDate date) {
        int dow = date.getDayOfWeek().getValue();
        return shiftRepo.findByDoctorIdAndDayOfWeek(doctor.getId(), dow).orElse(null);
    }

    public List<DoctorShift> shiftsFor(Long doctorId) {
        return shiftRepo.findByDoctorId(doctorId);
    }

    public Map<Integer, DoctorShift> shiftsByDay(Long doctorId) {
        return shiftRepo.findByDoctorId(doctorId).stream()
                .collect(Collectors.toMap(DoctorShift::getDayOfWeek, Function.identity(), (a, b) -> a));
    }

    @Transactional
    public Attendance checkIn(Long doctorId, LocalDate date) {
        Doctor doctor = resolveDoctor(doctorId);
        Attendance attendance = findOrCreate(doctor, date);
        if (attendance.getClockIn() == null) {
            attendance.setClockIn(LocalTime.now());
        }
        if (Attendance.ABSENT.equals(attendance.getStatus()) || Attendance.ON_LEAVE.equals(attendance.getStatus())) {
            attendance.setStatus(Attendance.PRESENT);
        }
        return attendanceRepo.save(attendance);
    }

    @Transactional
    public Attendance checkOut(Long doctorId, LocalDate date) {
        Doctor doctor = resolveDoctor(doctorId);
        Attendance attendance = findOrCreate(doctor, date);
        attendance.setClockOut(LocalTime.now());
        if (attendance.getClockIn() == null) {
            attendance.setClockIn(attendance.getClockOut());
        }
        if (Attendance.ABSENT.equals(attendance.getStatus()) || Attendance.ON_LEAVE.equals(attendance.getStatus())) {
            attendance.setStatus(Attendance.PRESENT);
        }
        return attendanceRepo.save(attendance);
    }

    @Transactional
    public Attendance markStatus(Long doctorId, LocalDate date, String status, String notes) {
        Doctor doctor = resolveDoctor(doctorId);
        Attendance attendance = findOrCreate(doctor, date);
        attendance.setStatus(status);
        if (notes != null && !notes.isBlank()) {
            attendance.setNotes(notes);
        }
        return attendanceRepo.save(attendance);
    }

    @Transactional
    public DoctorShift saveShift(Long doctorId, Integer dayOfWeek, LocalTime start, LocalTime end, String label) {
        Doctor doctor = resolveDoctor(doctorId);
        DoctorShift shift = shiftRepo.findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek)
                .orElseGet(() -> DoctorShift.builder().doctor(doctor).dayOfWeek(dayOfWeek).build());
        shift.setStartTime(start);
        shift.setEndTime(end);
        shift.setShiftLabel(label);
        return shiftRepo.save(shift);
    }

    public long presentCount(LocalDate date) {
        return attendanceRepo.countByAttendanceDateAndStatus(date, Attendance.PRESENT);
    }

    private Attendance findOrCreate(Doctor doctor, LocalDate date) {
        return attendanceRepo.findByDoctorIdAndAttendanceDate(doctor.getId(), date)
                .orElseGet(() -> Attendance.builder()
                        .doctor(doctor)
                        .attendanceDate(date)
                        .status(Attendance.PRESENT)
                        .build());
    }

    private Doctor resolveDoctor(Long doctorId) {
        return doctorRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
    }
}
