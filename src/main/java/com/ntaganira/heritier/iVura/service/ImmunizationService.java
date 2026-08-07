package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.ImmunizationDto;
import com.ntaganira.heritier.iVura.entity.Doctor;
import com.ntaganira.heritier.iVura.entity.Immunization;
import com.ntaganira.heritier.iVura.entity.Patient;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.ImmunizationRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.service
 * - File      : ImmunizationService.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Immunization Service
 * </pre>
 */
@Service
public class ImmunizationService {

    private final ImmunizationRepository immunizationRepo;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;

    public ImmunizationService(ImmunizationRepository immunizationRepo,
                               PatientRepository patientRepo,
                               DoctorRepository doctorRepo) {
        this.immunizationRepo = immunizationRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
    }

    public List<Immunization> findByPatientId(Long patientId) {
        return immunizationRepo.findByPatientIdOrderByAdministeredDateDesc(patientId);
    }

    public List<Immunization> findAll() {
        return immunizationRepo.findAllByOrderByAdministeredDateDesc();
    }

    public Immunization findById(Long id) {
        return immunizationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Immunization not found with id: " + id));
    }

    @Transactional
    public Immunization create(ImmunizationDto dto) {
        Patient patient = patientRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Doctor doctor = dto.getAdministeredById() != null
                ? doctorRepo.findById(dto.getAdministeredById()).orElse(null)
                : null;
        Immunization immunization = Immunization.builder()
                .patient(patient)
                .vaccine(dto.getVaccine())
                .doseNumber(dto.getDoseNumber() != null ? dto.getDoseNumber() : 1)
                .administeredDate(dto.getAdministeredDate() != null ? dto.getAdministeredDate() : LocalDate.now())
                .nextDueDate(dto.getNextDueDate())
                .administeredBy(doctor)
                .batchNumber(dto.getBatchNumber())
                .notes(dto.getNotes())
                .build();
        return immunizationRepo.save(immunization);
    }

    @Transactional
    public void delete(Long id) {
        immunizationRepo.delete(findById(id));
    }
}
