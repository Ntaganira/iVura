package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.SpecializationDto;
import com.ntaganira.heritier.iVura.entity.Specialization;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.SpecializationRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpecializationService {

    private final SpecializationRepository specializationRepo;
    private final DoctorRepository doctorRepo;

    public SpecializationService(SpecializationRepository specializationRepo, DoctorRepository doctorRepo) {
        this.specializationRepo = specializationRepo;
        this.doctorRepo = doctorRepo;
    }

    public Page<Specialization> findPage(String search, int page, int size) {
        Specification<Specialization> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (StringUtils.hasText(search)) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("name")), term),
                        cb.like(cb.lower(root.get("description")), term)
                ));
            }
            return predicate;
        };
        return specializationRepo.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    }

    public List<Specialization> findAll() {
        return specializationRepo.findAllByOrderByNameAsc();
    }

    public Specialization findById(Long id) {
        return specializationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Specialization not found with id: " + id));
    }

    @Transactional
    public Specialization create(SpecializationDto dto) {
        specializationRepo.findByNameIgnoreCase(dto.getName().trim())
                .ifPresent(s -> {
                    throw new RuntimeException("Specialization already exists: " + dto.getName());
                });
        Specialization specialization = Specialization.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .price(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO)
                .build();
        return specializationRepo.save(specialization);
    }

    @Transactional
    public Specialization update(Long id, SpecializationDto dto) {
        Specialization specialization = findById(id);
        specializationRepo.findByNameIgnoreCase(dto.getName().trim())
                .filter(s -> !s.getId().equals(id))
                .ifPresent(s -> {
                    throw new RuntimeException("Specialization already exists: " + dto.getName());
                });
        specialization.setName(dto.getName().trim());
        specialization.setDescription(dto.getDescription());
        specialization.setPrice(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO);
        return specializationRepo.save(specialization);
    }

    @Transactional
    public void delete(Long id) {
        Specialization specialization = findById(id);
        long doctorCount = doctorRepo.countBySpecializationId(id);
        if (doctorCount > 0) {
            throw new RuntimeException("Specialization is assigned to " + doctorCount + " doctor(s) and cannot be deleted");
        }
        specializationRepo.delete(specialization);
    }

    public long countDoctors() {
        return doctorRepo.countByIsActiveTrue();
    }

    public Map<Long, Long> doctorCounts() {
        return doctorRepo.findAll().stream()
                .filter(d -> d.getSpecialization() != null)
                .collect(Collectors.groupingBy(d -> d.getSpecialization().getId(), Collectors.counting()));
    }
}
