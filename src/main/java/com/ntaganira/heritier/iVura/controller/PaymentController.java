package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.PaymentDto;
import com.ntaganira.heritier.iVura.entity.Billing;
import com.ntaganira.heritier.iVura.entity.Payment;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private static final int PAGE_SIZE = 10;

    private final PaymentService paymentService;
    private final ActivityLogService activityLogService;

    public PaymentController(PaymentService paymentService, ActivityLogService activityLogService) {
        this.paymentService = paymentService;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_PAYMENT')")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String method,
                       @RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       Model model) {
        Page<Payment> payments = paymentService.findPage(search, method, from, to, page, PAGE_SIZE);
        model.addAttribute("payments", payments);
        model.addAttribute("search", search);
        model.addAttribute("selectedMethod", method);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("totalCollected", paymentService.totalCollected());
        model.addAttribute("collectedToday", paymentService.collectedToday());
        model.addAttribute("methodCounts", paymentService.methodCounts());
        model.addAttribute("paymentMethods", PaymentService.METHODS);
        model.addAttribute("paginationQuery", buildQuery(search, method, from, to));
        return "payments/list";
    }

    private String buildQuery(String search, String method, LocalDate from, LocalDate to) {
        StringBuilder query = new StringBuilder();
        if (org.springframework.util.StringUtils.hasText(search)) {
            query.append("search=")
                    .append(URLEncoder.encode(search.trim(), StandardCharsets.UTF_8));
        }
        if (org.springframework.util.StringUtils.hasText(method)) {
            appendParam(query, "method", method.trim());
        }
        if (from != null) {
            appendParam(query, "from", from.toString());
        }
        if (to != null) {
            appendParam(query, "to", to.toString());
        }
        return query.toString();
    }

    private void appendParam(StringBuilder query, String key, String value) {
        if (query.length() > 0) {
            query.append("&");
        }
        query.append(key).append("=")
                .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_PAYMENT')")
    public String addForm(@RequestParam(required = false) Long billingId, Model model) {
        PaymentDto dto = new PaymentDto();
        if (billingId != null) {
            dto.setBillingId(billingId);
        }
        model.addAttribute("paymentDto", dto);
        populateFormOptions(model, null);
        return "payments/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_PAYMENT')")
    public String create(@Valid @ModelAttribute("paymentDto") PaymentDto dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            populateFormOptions(model, null);
            model.addAttribute("formErrors", result.getFieldErrors());
            return "payments/form";
        }
        try {
            Payment payment = paymentService.create(dto);
            activityLogService.record("Payments", "CREATE_PAYMENT",
                    "Recorded payment of " + payment.getAmountPaid() + " RWF for "
                            + payment.getBilling().getInvoiceNumber(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Payment recorded successfully");
            return "redirect:/payments";
        } catch (RuntimeException e) {
            activityLogService.record("Payments", "CREATE_PAYMENT",
                    "Failed to record payment", ActivityStatus.FAILED);
            model.addAttribute("flashError", e.getMessage());
            populateFormOptions(model, null);
            return "payments/form";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_PAYMENT')")
    public String editForm(@PathVariable Long id, Model model) {
        Payment payment = paymentService.findById(id);
        PaymentDto dto = new PaymentDto();
        dto.setId(payment.getId());
        dto.setBillingId(payment.getBilling() != null ? payment.getBilling().getId() : null);
        dto.setAmountPaid(payment.getAmountPaid());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setReferenceNumber(payment.getReferenceNumber());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setNotes(payment.getNotes());
        model.addAttribute("paymentDto", dto);
        populateFormOptions(model, payment.getBilling());
        return "payments/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_PAYMENT')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("paymentDto") PaymentDto dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            dto.setId(id);
            populateFormOptions(model, null);
            model.addAttribute("formErrors", result.getFieldErrors());
            return "payments/form";
        }
        try {
            Payment payment = paymentService.update(id, dto);
            activityLogService.record("Payments", "EDIT_PAYMENT",
                    "Updated payment #" + payment.getId() + " for "
                            + payment.getBilling().getInvoiceNumber(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Payment updated successfully");
            return "redirect:/payments";
        } catch (RuntimeException e) {
            activityLogService.record("Payments", "EDIT_PAYMENT",
                    "Failed to update payment #" + id, ActivityStatus.FAILED);
            model.addAttribute("flashError", e.getMessage());
            populateFormOptions(model, null);
            return "payments/form";
        }
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_DELETE_PAYMENT')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Payment payment = paymentService.findById(id);
            String invoice = payment.getBilling() != null ? payment.getBilling().getInvoiceNumber() : null;
            paymentService.delete(id);
            activityLogService.record("Payments", "DELETE_PAYMENT",
                    "Deleted payment #" + id + (invoice != null ? " for " + invoice : ""),
                    ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Payment deleted successfully");
        } catch (RuntimeException e) {
            activityLogService.record("Payments", "DELETE_PAYMENT",
                    "Failed to delete payment #" + id, ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/payments";
    }

    private void populateFormOptions(Model model, Billing currentBilling) {
        List<Billing> bills = new ArrayList<>(paymentService.payableBills());
        if (currentBilling != null
                && bills.stream().noneMatch(b -> b.getId().equals(currentBilling.getId()))) {
            bills.add(0, currentBilling);
        }
        model.addAttribute("payableBills", bills);
        model.addAttribute("paymentMethods", PaymentService.METHODS);
    }
}
