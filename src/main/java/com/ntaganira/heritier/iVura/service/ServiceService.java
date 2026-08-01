package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.ServiceDto;
import com.ntaganira.heritier.iVura.entity.Service;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.ServiceRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceRepository serviceRepo;
    private final DoctorRepository doctorRepo;

    public ServiceService(ServiceRepository serviceRepo, DoctorRepository doctorRepo) {
        this.serviceRepo = serviceRepo;
        this.doctorRepo = doctorRepo;
    }

    public Page<Service> findPage(String search, int page, int size) {
        Specification<Service> spec = (root, query, cb) -> {
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
        return serviceRepo.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    }

    public List<Service> findAll() {
        return serviceRepo.findAllByOrderByNameAsc();
    }

    public Service findById(Long id) {
        return serviceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
    }

    @Transactional
    public Service create(ServiceDto dto) {
        serviceRepo.findByNameIgnoreCase(dto.getName().trim())
                .ifPresent(s -> {
                    throw new RuntimeException("Service already exists: " + dto.getName());
                });
        Service service = Service.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .price(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO)
                .build();
        return serviceRepo.save(service);
    }

    @Transactional
    public Service update(Long id, ServiceDto dto) {
        Service service = findById(id);
        serviceRepo.findByNameIgnoreCase(dto.getName().trim())
                .filter(s -> !s.getId().equals(id))
                .ifPresent(s -> {
                    throw new RuntimeException("Service already exists: " + dto.getName());
                });
        service.setName(dto.getName().trim());
        service.setDescription(dto.getDescription());
        service.setPrice(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO);
        return serviceRepo.save(service);
    }

    @Transactional
    public void delete(Long id) {
        Service service = findById(id);
        long doctorCount = doctorRepo.countByServiceId(id);
        if (doctorCount > 0) {
            throw new RuntimeException("Service is assigned to " + doctorCount + " doctor(s) and cannot be deleted");
        }
        serviceRepo.delete(service);
    }

    public long countDoctors() {
        return doctorRepo.countByIsActiveTrue();
    }

    public Map<Long, Long> doctorCounts() {
        return doctorRepo.findAll().stream()
                .filter(d -> d.getService() != null)
                .collect(Collectors.groupingBy(d -> d.getService().getId(), Collectors.counting()));
    }
}
