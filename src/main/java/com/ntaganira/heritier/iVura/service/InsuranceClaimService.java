package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.InsuranceClaimDto;
import com.ntaganira.heritier.iVura.entity.Billing;
import com.ntaganira.heritier.iVura.entity.InsuranceClaim;
import com.ntaganira.heritier.iVura.entity.Patient;
import com.ntaganira.heritier.iVura.repository.BillingRepository;
import com.ntaganira.heritier.iVura.repository.InsuranceClaimRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.service
 * - File      : InsuranceClaimService.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Insurance claim processing service
 * </pre>
 */
@Service
public class InsuranceClaimService {

    public static final List<String> STATUSES =
            List.of("SUBMITTED", "APPROVED", "REJECTED", "PAID");

    private final InsuranceClaimRepository claimRepo;
    private final PatientRepository patientRepo;
    private final BillingRepository billingRepo;
    private final NotificationService notificationService;

    public InsuranceClaimService(InsuranceClaimRepository claimRepo,
                                 PatientRepository patientRepo,
                                 BillingRepository billingRepo,
                                 NotificationService notificationService) {
        this.claimRepo = claimRepo;
        this.patientRepo = patientRepo;
        this.billingRepo = billingRepo;
        this.notificationService = notificationService;
    }

    public Page<InsuranceClaim> findPage(String search, String status, int page, int size) {
        Specification<InsuranceClaim> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (StringUtils.hasText(search)) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("claimNumber")), term),
                        cb.like(cb.lower(root.get("provider")), term),
                        cb.like(cb.lower(root.get("patient").get("firstName")), term),
                        cb.like(cb.lower(root.get("patient").get("lastName")), term)
                ));
            }
            if (StringUtils.hasText(status) && STATUSES.contains(status.trim().toUpperCase())) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status.trim().toUpperCase()));
            }
            return predicate;
        };
        return claimRepo.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedDate")));
    }

    public InsuranceClaim findById(Long id) {
        return claimRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + id));
    }

    public List<InsuranceClaim> findByPatient(Long patientId) {
        return claimRepo.findByPatientIdOrderBySubmittedDateDesc(patientId);
    }

    @Transactional
    public InsuranceClaim create(InsuranceClaimDto dto) {
        Patient patient = patientRepo.findById(dto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        BigDecimal amount = dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO;
        Billing billing = dto.getBillingId() != null
                ? billingRepo.findById(dto.getBillingId()).orElse(null)
                : null;

        InsuranceClaim claim = InsuranceClaim.builder()
                .claimNumber(generateClaimNumber())
                .patient(patient)
                .billing(billing)
                .provider(dto.getProvider())
                .policyNumber(dto.getPolicyNumber())
                .amount(amount)
                .status("SUBMITTED")
                .submittedDate(LocalDate.now())
                .remarks(dto.getRemarks())
                .build();
        InsuranceClaim saved = claimRepo.save(claim);
        notificationService.notifyAll(
                "New insurance claim",
                "Claim " + saved.getClaimNumber() + " of " + saved.getAmount() + " RWF submitted for "
                        + patient.getFullName(),
                NotificationService.TYPE_BILL);
        return saved;
    }

    @Transactional
    public InsuranceClaim updateStatus(Long id, String status, String remarks) {
        InsuranceClaim claim = findById(id);
        String normalized = StringUtils.hasText(status) && STATUSES.contains(status.trim().toUpperCase())
                ? status.trim().toUpperCase() : claim.getStatus();
        claim.setStatus(normalized);
        if ("APPROVED".equals(normalized) || "REJECTED".equals(normalized) || "PAID".equals(normalized)) {
            claim.setDecisionDate(LocalDate.now());
        }
        if (StringUtils.hasText(remarks)) {
            claim.setRemarks(remarks);
        }
        InsuranceClaim saved = claimRepo.save(claim);
        notificationService.notifyAll(
                "Claim " + normalized.toLowerCase(),
                "Claim " + saved.getClaimNumber() + " for " + saved.getPatient().getFullName()
                        + " was " + normalized.toLowerCase(),
                NotificationService.TYPE_BILL);
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        claimRepo.delete(findById(id));
    }

    public Map<String, Long> statusCounts() {
        return STATUSES.stream()
                .collect(Collectors.toMap(s -> s, claimRepo::countByStatus,
                        (a, b) -> a, java.util.LinkedHashMap::new));
    }

    public BigDecimal totalClaimed() {
        return claimRepo.findAll().stream()
                .map(InsuranceClaim::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateClaimNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "CLM-" + year + "-";
        long seq = 1;
        InsuranceClaim last = claimRepo.findTopByOrderByIdDesc();
        if (last != null && last.getClaimNumber() != null
                && last.getClaimNumber().startsWith(prefix)) {
            try {
                seq = Long.parseLong(last.getClaimNumber().substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                // fall back to 1 if the suffix is not numeric
            }
        }
        return String.format("%s%06d", prefix, seq);
    }
}
