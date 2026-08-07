package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.DoctorDto;
import com.ntaganira.heritier.iVura.entity.Department;
import com.ntaganira.heritier.iVura.entity.Doctor;
import com.ntaganira.heritier.iVura.entity.Service;
import com.ntaganira.heritier.iVura.entity.Specialization;
import com.ntaganira.heritier.iVura.repository.DepartmentRepository;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.ServiceRepository;
import com.ntaganira.heritier.iVura.repository.SpecializationRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class DoctorService {

    private final DoctorRepository doctorRepo;
    private final DepartmentRepository departmentRepo;
    private final SpecializationRepository specializationRepo;
    private final ServiceRepository serviceRepo;

    public DoctorService(DoctorRepository doctorRepo, DepartmentRepository departmentRepo,
                         SpecializationRepository specializationRepo, ServiceRepository serviceRepo) {
        this.doctorRepo = doctorRepo;
        this.departmentRepo = departmentRepo;
        this.specializationRepo = specializationRepo;
        this.serviceRepo = serviceRepo;
    }

    public List<Doctor> findAll() {
        return doctorRepo.findAll();
    }

    public Doctor findById(Long id) {
        return doctorRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));
    }

    public Doctor create(DoctorDto dto) {
        Department dept = dto.getDepartmentId() != null
            ? departmentRepo.findById(dto.getDepartmentId()).orElse(null) : null;

        Doctor doctor = Doctor.builder()
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .email(dto.getEmail())
            .phone(dto.getPhone())
            .specializations(resolveSpecializations(dto.getSpecializationIds()))
            .services(resolveServices(dto.getServiceIds()))
            .licenseNumber(dto.getLicenseNumber())
            .department(dept)
            .qualification(dto.getQualification())
            .experienceYears(dto.getExperienceYears())
            .availableFrom(dto.getAvailableFrom())
            .availableTo(dto.getAvailableTo())
            .isActive(true)
            .build();
        return doctorRepo.save(doctor);
    }

    public Doctor update(Long id, DoctorDto dto) {
        Doctor doctor = findById(id);
        Department dept = dto.getDepartmentId() != null
            ? departmentRepo.findById(dto.getDepartmentId()).orElse(null) : null;

        doctor.setFirstName(dto.getFirstName());
        doctor.setLastName(dto.getLastName());
        doctor.setEmail(dto.getEmail());
        doctor.setPhone(dto.getPhone());
        doctor.setSpecializations(resolveSpecializations(dto.getSpecializationIds()));
        doctor.setServices(resolveServices(dto.getServiceIds()));
        doctor.setLicenseNumber(dto.getLicenseNumber());
        doctor.setDepartment(dept);
        doctor.setQualification(dto.getQualification());
        doctor.setExperienceYears(dto.getExperienceYears());
        doctor.setAvailableFrom(dto.getAvailableFrom());
        doctor.setAvailableTo(dto.getAvailableTo());
        return doctorRepo.save(doctor);
    }

    private Set<Specialization> resolveSpecializations(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .map(id -> specializationRepo.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Service> resolveServices(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .map(id -> serviceRepo.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public void delete(Long id) {
        Doctor doctor = findById(id);
        doctor.setIsActive(false);
        doctorRepo.save(doctor);
    }
}
