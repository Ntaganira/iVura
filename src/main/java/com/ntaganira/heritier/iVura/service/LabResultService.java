package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.LabResultDto;
import com.ntaganira.heritier.iVura.entity.Doctor;
import com.ntaganira.heritier.iVura.entity.LabResult;
import com.ntaganira.heritier.iVura.entity.Patient;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.LabResultRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.service
 * - File      : LabResultService.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Laboratory Result Service
 * </pre>
 */
@Service
public class LabResultService {

    public static final List<String> STATUSES =
            List.of("PENDING", "COMPLETED", "ABNORMAL", "CANCELLED");

    private final LabResultRepository labRepo;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;

    public LabResultService(LabResultRepository labRepo,
                            PatientRepository patientRepo,
                            DoctorRepository doctorRepo) {
        this.labRepo = labRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
    }

    public List<LabResult> findAll() {
        return labRepo.findAllByOrderByPerformedAtDesc();
    }

    public List<LabResult> findByStatus(String status) {
        return StringUtils.hasText(status) && STATUSES.contains(status)
                ? labRepo.findByStatusOrderByPerformedAtDesc(status)
                : findAll();
    }

    public List<LabResult> findByPatientId(Long patientId) {
        return labRepo.findByPatientIdOrderByPerformedAtDesc(patientId);
    }

    public LabResult findById(Long id) {
        return labRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lab result not found with id: " + id));
    }

    public Map<String, Long> statusCounts() {
        return STATUSES.stream()
                .collect(Collectors.toMap(s -> s, labRepo::countByStatus,
                        (a, b) -> a, java.util.LinkedHashMap::new));
    }

    @Transactional
    public LabResult create(LabResultDto dto) {
        Patient patient = patientRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Doctor doctor = dto.getDoctorId() != null
                ? doctorRepo.findById(dto.getDoctorId()).orElse(null)
                : null;
        LabResult lab = LabResult.builder()
                .patient(patient)
                .doctor(doctor)
                .testName(dto.getTestName())
                .category(dto.getCategory())
                .result(dto.getResult())
                .unit(dto.getUnit())
                .normalRange(dto.getNormalRange())
                .status(normalizeStatus(dto.getStatus()))
                .notes(dto.getNotes())
                .build();
        return labRepo.save(lab);
    }

    @Transactional
    public LabResult update(Long id, LabResultDto dto) {
        LabResult lab = findById(id);
        lab.setPatient(patientRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found")));
        lab.setDoctor(dto.getDoctorId() != null
                ? doctorRepo.findById(dto.getDoctorId()).orElse(null)
                : null);
        lab.setTestName(dto.getTestName());
        lab.setCategory(dto.getCategory());
        lab.setResult(dto.getResult());
        lab.setUnit(dto.getUnit());
        lab.setNormalRange(dto.getNormalRange());
        lab.setStatus(normalizeStatus(dto.getStatus()));
        lab.setNotes(dto.getNotes());
        return labRepo.save(lab);
    }

    @Transactional
    public LabResult updateStatus(Long id, String status) {
        LabResult lab = findById(id);
        lab.setStatus(normalizeStatus(status));
        return labRepo.save(lab);
    }

    @Transactional
    public void delete(Long id) {
        labRepo.delete(findById(id));
    }

    private String normalizeStatus(String status) {
        return StringUtils.hasText(status) && STATUSES.contains(status.trim().toUpperCase())
                ? status.trim().toUpperCase()
                : "PENDING";
    }
}
