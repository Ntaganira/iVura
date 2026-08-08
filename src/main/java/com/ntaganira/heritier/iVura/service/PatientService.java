package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.PatientDto;
import com.ntaganira.heritier.iVura.entity.Patient;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepo;

    public PatientService(PatientRepository patientRepo) {
        this.patientRepo = patientRepo;
    }

    public List<Patient> findAll() {
        return patientRepo.findAll();
    }

    public Patient findById(Long id) {
        return patientRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Patient not found with id: " + id));
    }

    public Patient create(PatientDto dto) {
        Patient patient = Patient.builder()
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .email(dto.getEmail())
            .phone(dto.getPhone())
            .dateOfBirth(dto.getDateOfBirth())
            .gender(dto.getGender())
            .bloodGroup(dto.getBloodGroup())
            .address(dto.getAddress())
            .city(dto.getCity())
            .state(dto.getState())
            .zipCode(dto.getZipCode())
            .emergencyContactName(dto.getEmergencyContactName())
            .emergencyContactPhone(dto.getEmergencyContactPhone())
            .medicalHistory(dto.getMedicalHistory())
            .allergies(dto.getAllergies())
            .hasInsurance(Boolean.TRUE.equals(dto.getHasInsurance()))
            .insuranceProvider(dto.getInsuranceProvider())
            .insurancePolicyNumber(dto.getInsurancePolicyNumber())
            .insuranceMemberName(dto.getInsuranceMemberName())
            .insuranceExpiryDate(dto.getInsuranceExpiryDate())
            .signatureData(dto.getSignatureData())
            .consentGiven(Boolean.TRUE.equals(dto.getConsentGiven()))
            .consentDate(Boolean.TRUE.equals(dto.getConsentGiven()) ? LocalDateTime.now() : null)
            .isActive(true)
            .build();
        return patientRepo.save(patient);
    }

    public Patient update(Long id, PatientDto dto) {
        Patient patient = findById(id);
        patient.setFirstName(dto.getFirstName());
        patient.setLastName(dto.getLastName());
        patient.setEmail(dto.getEmail());
        patient.setPhone(dto.getPhone());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setBloodGroup(dto.getBloodGroup());
        patient.setAddress(dto.getAddress());
        patient.setCity(dto.getCity());
        patient.setState(dto.getState());
        patient.setZipCode(dto.getZipCode());
        patient.setEmergencyContactName(dto.getEmergencyContactName());
        patient.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        patient.setMedicalHistory(dto.getMedicalHistory());
        patient.setAllergies(dto.getAllergies());
        patient.setHasInsurance(Boolean.TRUE.equals(dto.getHasInsurance()));
        patient.setInsuranceProvider(dto.getInsuranceProvider());
        patient.setInsurancePolicyNumber(dto.getInsurancePolicyNumber());
        patient.setInsuranceMemberName(dto.getInsuranceMemberName());
        patient.setInsuranceExpiryDate(dto.getInsuranceExpiryDate());
        boolean consent = Boolean.TRUE.equals(dto.getConsentGiven());
        if (consent && !Boolean.TRUE.equals(patient.getConsentGiven())) {
            patient.setConsentDate(LocalDateTime.now());
        }
        patient.setConsentGiven(consent);
        if (dto.getSignatureData() != null && !dto.getSignatureData().isBlank()) {
            patient.setSignatureData(dto.getSignatureData());
        }
        return patientRepo.save(patient);
    }

    public void delete(Long id) {
        Patient patient = findById(id);
        patient.setIsActive(false);
        patientRepo.save(patient);
    }
}
