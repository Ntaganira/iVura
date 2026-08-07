package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.BillingDto;
import com.ntaganira.heritier.iVura.entity.Appointment;
import com.ntaganira.heritier.iVura.entity.Billing;
import com.ntaganira.heritier.iVura.entity.Patient;
import com.ntaganira.heritier.iVura.repository.AppointmentRepository;
import com.ntaganira.heritier.iVura.repository.BillingRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class BillingService {

    private static final List<String> STATUSES =
            List.of("PENDING", "PAID", "PARTIALLY_PAID", "CANCELLED", "REFUNDED");

    private final BillingRepository billingRepo;
    private final PatientRepository patientRepo;
    private final AppointmentRepository appointmentRepo;

    public BillingService(BillingRepository billingRepo, PatientRepository patientRepo,
                          AppointmentRepository appointmentRepo) {
        this.billingRepo = billingRepo;
        this.patientRepo = patientRepo;
        this.appointmentRepo = appointmentRepo;
    }

    public Page<Billing> findPage(String search, String status, int page, int size) {
        Specification<Billing> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (StringUtils.hasText(search)) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("invoiceNumber")), term),
                        cb.like(cb.lower(root.get("patient").get("firstName")), term),
                        cb.like(cb.lower(root.get("patient").get("lastName")), term)
                ));
            }
            if (StringUtils.hasText(status) && STATUSES.contains(status.trim().toUpperCase())) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status.trim().toUpperCase()));
            }
            return predicate;
        };
        return billingRepo.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public Billing findById(Long id) {
        return billingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bill not found with id: " + id));
    }

    @Transactional
    public Billing create(BillingDto dto) {
        Patient patient = resolvePatient(dto.getPatientId());
        BigDecimal amount = dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO;
        BigDecimal tax = dto.getTax() != null ? dto.getTax() : BigDecimal.ZERO;
        BigDecimal discount = dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO;
        String status = normalizeStatus(dto.getStatus());

        Billing billing = Billing.builder()
                .patient(patient)
                .appointment(resolveAppointment(dto.getAppointmentId()))
                .invoiceNumber(generateInvoiceNumber())
                .amount(amount)
                .tax(tax)
                .discount(discount)
                .totalAmount(computeTotal(amount, tax, discount))
                .status(status)
                .paymentMethod(dto.getPaymentMethod())
                .notes(dto.getNotes())
                .build();
        if ("PAID".equals(status)) {
            billing.setPaymentDate(LocalDateTime.now());
        }
        return billingRepo.save(billing);
    }

    @Transactional
    public Billing update(Long id, BillingDto dto) {
        Billing billing = findById(id);
        BigDecimal amount = dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO;
        BigDecimal tax = dto.getTax() != null ? dto.getTax() : BigDecimal.ZERO;
        BigDecimal discount = dto.getDiscount() != null ? dto.getDiscount() : BigDecimal.ZERO;
        String status = normalizeStatus(dto.getStatus());

        billing.setPatient(resolvePatient(dto.getPatientId()));
        billing.setAppointment(resolveAppointment(dto.getAppointmentId()));
        billing.setAmount(amount);
        billing.setTax(tax);
        billing.setDiscount(discount);
        billing.setTotalAmount(computeTotal(amount, tax, discount));
        billing.setStatus(status);
        billing.setPaymentMethod(dto.getPaymentMethod());
        billing.setNotes(dto.getNotes());
        if ("PAID".equals(status) && billing.getPaymentDate() == null) {
            billing.setPaymentDate(LocalDateTime.now());
        }
        return billingRepo.save(billing);
    }

    @Transactional
    public Billing markPaid(Long id, String paymentMethod) {
        Billing billing = findById(id);
        if ("CANCELLED".equals(billing.getStatus()) || "REFUNDED".equals(billing.getStatus())) {
            throw new RuntimeException("A " + billing.getStatus().toLowerCase() + " bill cannot be marked as paid");
        }
        billing.setStatus("PAID");
        billing.setPaymentMethod(StringUtils.hasText(paymentMethod)
                ? paymentMethod : billing.getPaymentMethod());
        billing.setPaymentDate(LocalDateTime.now());
        return billingRepo.save(billing);
    }

    @Transactional
    public void delete(Long id) {
        billingRepo.delete(findById(id));
    }

    public Map<String, Long> statusCounts() {
        return STATUSES.stream()
                .collect(Collectors.toMap(s -> s, billingRepo::countByStatus,
                        (a, b) -> a, java.util.LinkedHashMap::new));
    }

    public BigDecimal totalRevenue() {
        return billingRepo.findByStatus("PAID").stream()
                .map(Billing::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Patient resolvePatient(Long patientId) {
        return patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    private Appointment resolveAppointment(Long appointmentId) {
        return appointmentId != null
                ? appointmentRepo.findById(appointmentId).orElse(null)
                : null;
    }

    private BigDecimal computeTotal(BigDecimal amount, BigDecimal tax, BigDecimal discount) {
        return amount.add(tax).subtract(discount).max(BigDecimal.ZERO);
    }

    private String normalizeStatus(String status) {
        return StringUtils.hasText(status) && STATUSES.contains(status.trim().toUpperCase())
                ? status.trim().toUpperCase()
                : "PENDING";
    }

    private String generateInvoiceNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "INV-" + year + "-";
        long seq = 1;
        Billing last = billingRepo.findTopByOrderByIdDesc();
        if (last != null && last.getInvoiceNumber() != null
                && last.getInvoiceNumber().startsWith(prefix)) {
            try {
                seq = Long.parseLong(last.getInvoiceNumber().substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                // fall back to 1 if the suffix is not numeric
            }
        }
        return String.format("%s%06d", prefix, seq);
    }
}
