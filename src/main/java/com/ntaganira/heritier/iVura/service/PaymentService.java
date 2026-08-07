package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.PaymentDto;
import com.ntaganira.heritier.iVura.entity.Billing;
import com.ntaganira.heritier.iVura.entity.Payment;
import com.ntaganira.heritier.iVura.entity.User;
import com.ntaganira.heritier.iVura.repository.BillingRepository;
import com.ntaganira.heritier.iVura.repository.PaymentRepository;
import com.ntaganira.heritier.iVura.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class PaymentService {

    public static final List<String> METHODS =
            List.of("CASH", "CARD", "MOBILE_MONEY", "BANK_TRANSFER", "INSURANCE");

    private final PaymentRepository paymentRepo;
    private final BillingRepository billingRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    public PaymentService(PaymentRepository paymentRepo, BillingRepository billingRepo,
                          UserRepository userRepo, NotificationService notificationService) {
        this.paymentRepo = paymentRepo;
        this.billingRepo = billingRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    public Page<Payment> findPage(String search, String method, LocalDate from, LocalDate to,
                                  int page, int size) {
        Specification<Payment> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            if (StringUtils.hasText(search)) {
                String term = "%" + search.trim().toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("billing").get("invoiceNumber")), term),
                        cb.like(cb.lower(root.get("billing").get("patient").get("firstName")), term),
                        cb.like(cb.lower(root.get("billing").get("patient").get("lastName")), term),
                        cb.like(cb.lower(root.get("referenceNumber")), term)
                ));
            }
            if (StringUtils.hasText(method) && METHODS.contains(method.trim().toUpperCase())) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("paymentMethod"), method.trim().toUpperCase()));
            }
            if (from != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("paymentDate"), from.atStartOfDay()));
            }
            if (to != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("paymentDate"), to.atTime(LocalTime.MAX)));
            }
            return predicate;
        };
        return paymentRepo.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate")));
    }

    public Payment findById(Long id) {
        return paymentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    public BigDecimal paidSoFar(Long billingId) {
        return paymentRepo.sumByBillingId(billingId);
    }

    public List<Billing> payableBills() {
        return billingRepo.findByStatusIn(List.of("PENDING", "PARTIALLY_PAID"));
    }

    public BigDecimal totalCollected() {
        return paymentRepo.sumBetween(LocalDateTime.of(1900, 1, 1, 0, 0), LocalDateTime.now());
    }

    public BigDecimal collectedToday() {
        return paymentRepo.sumBetween(LocalDate.now().atStartOfDay(), LocalDateTime.now());
    }

    public Map<String, Long> methodCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String m : METHODS) {
            counts.put(m, 0L);
        }
        for (Object[] row : paymentRepo.countByMethod()) {
            String method = row[0] != null ? (String) row[0] : "UNKNOWN";
            counts.put(method, (Long) row[1]);
        }
        return counts;
    }

    @Transactional
    public Payment create(PaymentDto dto) {
        Billing billing = billingRepo.findById(dto.getBillingId())
                .orElseThrow(() -> new RuntimeException("Bill not found"));
        if ("CANCELLED".equals(billing.getStatus()) || "REFUNDED".equals(billing.getStatus())) {
            throw new RuntimeException("Cannot record a payment on a " + billing.getStatus().toLowerCase() + " bill");
        }
        BigDecimal amount = dto.getAmountPaid();
        BigDecimal remaining = billing.getTotalAmount().subtract(paidSoFar(billing.getId()));
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }
        if (amount.compareTo(remaining) > 0) {
            throw new RuntimeException("Payment of " + amount + " RWF exceeds the remaining balance of "
                    + remaining + " RWF");
        }

        Payment payment = Payment.builder()
                .billing(billing)
                .amountPaid(amount)
                .paymentMethod(normalizeMethod(dto.getPaymentMethod()))
                .referenceNumber(dto.getReferenceNumber())
                .paymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : LocalDateTime.now())
                .notes(dto.getNotes())
                .recordedBy(currentUser())
                .build();
        paymentRepo.save(payment);
        refreshBillingStatus(billing.getId());
        notificationService.notifyAll(
                "Payment recorded",
                "Payment of " + amount + " RWF received against " + billing.getInvoiceNumber()
                        + (billing.getPatient() != null ? " (" + billing.getPatient().getFullName() + ")" : ""),
                NotificationService.TYPE_PAYMENT);
        return payment;
    }

    @Transactional
    public Payment update(Long id, PaymentDto dto) {
        Payment payment = findById(id);
        Billing billing = payment.getBilling();
        if (!billing.getId().equals(dto.getBillingId())) {
            throw new RuntimeException("A payment cannot be moved to a different bill");
        }
        BigDecimal amount = dto.getAmountPaid();
        BigDecimal otherPayments = paidSoFar(billing.getId()).subtract(payment.getAmountPaid());
        BigDecimal remaining = billing.getTotalAmount().subtract(otherPayments);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }
        if (amount.compareTo(remaining) > 0) {
            throw new RuntimeException("Payment of " + amount + " RWF exceeds the remaining balance of "
                    + remaining + " RWF");
        }

        payment.setAmountPaid(amount);
        payment.setPaymentMethod(normalizeMethod(dto.getPaymentMethod()));
        payment.setReferenceNumber(dto.getReferenceNumber());
        payment.setNotes(dto.getNotes());
        if (dto.getPaymentDate() != null) {
            payment.setPaymentDate(dto.getPaymentDate());
        }
        paymentRepo.save(payment);
        refreshBillingStatus(billing.getId());
        return payment;
    }

    @Transactional
    public void delete(Long id) {
        Payment payment = findById(id);
        Long billingId = payment.getBilling().getId();
        paymentRepo.delete(payment);
        refreshBillingStatus(billingId);
    }

    private void refreshBillingStatus(Long billingId) {
        Billing billing = billingRepo.findById(billingId).orElse(null);
        if (billing == null) {
            return;
        }
        BigDecimal paid = paidSoFar(billingId);
        if (paid.compareTo(billing.getTotalAmount()) >= 0) {
            billing.setStatus("PAID");
            billing.setPaymentMethod(latestPaymentMethod(billingId));
            billing.setPaymentDate(LocalDateTime.now());
        } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
            billing.setStatus("PARTIALLY_PAID");
            billing.setPaymentDate(null);
        } else {
            billing.setStatus("PENDING");
            billing.setPaymentMethod(null);
            billing.setPaymentDate(null);
        }
        billingRepo.save(billing);
    }

    private String latestPaymentMethod(Long billingId) {
        return paymentRepo.findTopByBillingIdOrderByPaymentDateDesc(billingId)
                .map(Payment::getPaymentMethod)
                .orElse(null);
    }

    private String normalizeMethod(String method) {
        return StringUtils.hasText(method) && METHODS.contains(method.trim().toUpperCase())
                ? method.trim().toUpperCase()
                : null;
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            return userRepo.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
}
