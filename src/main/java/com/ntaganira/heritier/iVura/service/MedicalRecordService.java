package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.MedicalRecordDto;
import com.ntaganira.heritier.iVura.entity.Appointment;
import com.ntaganira.heritier.iVura.entity.Doctor;
import com.ntaganira.heritier.iVura.entity.MedicalRecord;
import com.ntaganira.heritier.iVura.entity.Patient;
import com.ntaganira.heritier.iVura.repository.AppointmentRepository;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.MedicalRecordRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.service
 * - File      : MedicalRecordService.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Medical Record Service
 * </pre>
 */
@Service
public class MedicalRecordService {

    private final MedicalRecordRepository recordRepo;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final AppointmentRepository appointmentRepo;

    public MedicalRecordService(MedicalRecordRepository recordRepo,
                                PatientRepository patientRepo,
                                DoctorRepository doctorRepo,
                                AppointmentRepository appointmentRepo) {
        this.recordRepo = recordRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.appointmentRepo = appointmentRepo;
    }

    public Page<MedicalRecord> findPage(int page, int size) {
        return recordRepo.findAll(PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "recordDate", "createdAt")));
    }

    public List<MedicalRecord> findByPatientId(Long patientId) {
        return recordRepo.findByPatientId(patientId);
    }

    public MedicalRecord findById(Long id) {
        return recordRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical record not found with id: " + id));
    }

    @Transactional
    public MedicalRecord create(MedicalRecordDto dto) {
        Patient patient = patientRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Doctor doctor = dto.getDoctorId() != null
                ? doctorRepo.findById(dto.getDoctorId()).orElse(null)
                : null;
        Appointment appointment = dto.getAppointmentId() != null
                ? appointmentRepo.findById(dto.getAppointmentId()).orElse(null)
                : null;
        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .diagnosis(dto.getDiagnosis())
                .prescription(dto.getPrescription())
                .notes(dto.getNotes())
                .recordDate(dto.getRecordDate() != null ? dto.getRecordDate() : LocalDate.now())
                .build();
        return recordRepo.save(record);
    }

    @Transactional
    public MedicalRecord update(Long id, MedicalRecordDto dto) {
        MedicalRecord record = findById(id);
        record.setPatient(patientRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found")));
        record.setDoctor(dto.getDoctorId() != null
                ? doctorRepo.findById(dto.getDoctorId()).orElse(null)
                : null);
        record.setAppointment(dto.getAppointmentId() != null
                ? appointmentRepo.findById(dto.getAppointmentId()).orElse(null)
                : null);
        record.setDiagnosis(dto.getDiagnosis());
        record.setPrescription(dto.getPrescription());
        record.setNotes(dto.getNotes());
        if (dto.getRecordDate() != null) {
            record.setRecordDate(dto.getRecordDate());
        }
        return recordRepo.save(record);
    }

    @Transactional
    public void delete(Long id) {
        recordRepo.delete(findById(id));
    }
}
