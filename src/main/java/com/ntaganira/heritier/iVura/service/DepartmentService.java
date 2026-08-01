package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.DepartmentDto;
import com.ntaganira.heritier.iVura.entity.Department;
import com.ntaganira.heritier.iVura.repository.DepartmentRepository;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepo;
    private final DoctorRepository doctorRepo;

    public DepartmentService(DepartmentRepository departmentRepo, DoctorRepository doctorRepo) {
        this.departmentRepo = departmentRepo;
        this.doctorRepo = doctorRepo;
    }

    public Page<Department> findPage(String search, int page, int size) {
        Specification<Department> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (StringUtils.hasText(search)) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("name")), term),
                        cb.like(cb.lower(root.get("location")), term)
                ));
            }
            return predicate;
        };
        return departmentRepo.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    }

    public List<Department> findAll() {
        return departmentRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    public Department findById(Long id) {
        return departmentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
    }

    @Transactional
    public Department create(DepartmentDto dto) {
        departmentRepo.findByNameIgnoreCase(dto.getName().trim())
                .ifPresent(d -> {
                    throw new RuntimeException("Department name already exists: " + dto.getName());
                });
        Department department = Department.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .phone(dto.getPhone())
                .location(dto.getLocation())
                .build();
        return departmentRepo.save(department);
    }

    @Transactional
    public Department update(Long id, DepartmentDto dto) {
        Department department = findById(id);
        departmentRepo.findByNameIgnoreCase(dto.getName().trim())
                .filter(d -> !d.getId().equals(id))
                .ifPresent(d -> {
                    throw new RuntimeException("Department name already exists: " + dto.getName());
                });
        department.setName(dto.getName().trim());
        department.setDescription(dto.getDescription());
        department.setPhone(dto.getPhone());
        department.setLocation(dto.getLocation());
        return departmentRepo.save(department);
    }

    @Transactional
    public void delete(Long id) {
        Department department = findById(id);
        long doctorCount = doctorRepo.countByDepartmentId(id);
        if (doctorCount > 0) {
            throw new RuntimeException("Department has " + doctorCount + " doctor(s) assigned and cannot be deleted");
        }
        departmentRepo.delete(department);
    }

    public long countDoctors() {
        return doctorRepo.countByIsActiveTrue();
    }

    public Map<Long, Long> doctorCounts() {
        return doctorRepo.findAll().stream()
                .filter(d -> d.getDepartment() != null)
                .collect(Collectors.groupingBy(d -> d.getDepartment().getId(), Collectors.counting()));
    }
}
