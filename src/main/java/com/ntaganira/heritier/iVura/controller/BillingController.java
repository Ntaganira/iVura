package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.BillingDto;
import com.ntaganira.heritier.iVura.entity.Billing;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.AppointmentRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/billings")
public class BillingController {

    private static final int PAGE_SIZE = 10;

    private final BillingService billingService;
    private final PatientRepository patientRepo;
    private final AppointmentRepository appointmentRepo;
    private final ActivityLogService activityLogService;

    public BillingController(BillingService billingService, PatientRepository patientRepo,
                             AppointmentRepository appointmentRepo,
                             ActivityLogService activityLogService) {
        this.billingService = billingService;
        this.patientRepo = patientRepo;
        this.appointmentRepo = appointmentRepo;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_BILL')")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String status,
                       Model model) {
        Page<Billing> bills = billingService.findPage(search, status, page, PAGE_SIZE);
        model.addAttribute("bills", bills);
        model.addAttribute("search", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statusCounts", billingService.statusCounts());
        model.addAttribute("totalRevenue", billingService.totalRevenue());
        model.addAttribute("paginationQuery", buildQuery(search, status));
        return "billings/list";
    }

    private String buildQuery(String search, String status) {
        StringBuilder query = new StringBuilder();
        if (org.springframework.util.StringUtils.hasText(search)) {
            query.append("search=")
                    .append(URLEncoder.encode(search.trim(), StandardCharsets.UTF_8));
        }
        if (org.springframework.util.StringUtils.hasText(status)) {
            if (query.length() > 0) {
                query.append("&");
            }
            query.append("status=")
                    .append(URLEncoder.encode(status.trim(), StandardCharsets.UTF_8));
        }
        return query.toString();
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_BILL')")
    public String addForm(Model model) {
        model.addAttribute("billingDto", new BillingDto());
        populateFormOptions(model);
        return "billings/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_BILL')")
    public String create(@Valid @ModelAttribute("billingDto") BillingDto dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            populateFormOptions(model);
            model.addAttribute("formErrors", result.getFieldErrors());
            return "billings/form";
        }
        try {
            Billing billing = billingService.create(dto);
            activityLogService.record("Billing", "CREATE_BILL",
                    "Created bill " + billing.getInvoiceNumber(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Bill created successfully");
            return "redirect:/billings";
        } catch (RuntimeException e) {
            activityLogService.record("Billing", "CREATE_BILL",
                    "Failed to create bill", ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/billings/add";
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_BILL')")
    public String editForm(@PathVariable Long id, Model model) {
        Billing billing = billingService.findById(id);
        BillingDto dto = new BillingDto();
        dto.setId(billing.getId());
        dto.setPatientId(billing.getPatient() != null ? billing.getPatient().getId() : null);
        dto.setAppointmentId(billing.getAppointment() != null ? billing.getAppointment().getId() : null);
        dto.setInvoiceNumber(billing.getInvoiceNumber());
        dto.setAmount(billing.getAmount());
        dto.setTax(billing.getTax());
        dto.setDiscount(billing.getDiscount());
        dto.setTotalAmount(billing.getTotalAmount());
        dto.setStatus(billing.getStatus());
        dto.setPaymentMethod(billing.getPaymentMethod());
        dto.setPaymentDate(billing.getPaymentDate());
        dto.setNotes(billing.getNotes());
        model.addAttribute("billingDto", dto);
        populateFormOptions(model);
        return "billings/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_BILL')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("billingDto") BillingDto dto,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            dto.setId(id);
            populateFormOptions(model);
            model.addAttribute("formErrors", result.getFieldErrors());
            return "billings/form";
        }
        try {
            Billing billing = billingService.update(id, dto);
            activityLogService.record("Billing", "EDIT_BILL",
                    "Updated bill " + billing.getInvoiceNumber(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Bill updated successfully");
            return "redirect:/billings";
        } catch (RuntimeException e) {
            activityLogService.record("Billing", "EDIT_BILL",
                    "Failed to update bill #" + id, ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
            return "redirect:/billings/edit/" + id;
        }
    }

    @GetMapping("/pay/{id}")
    @PreAuthorize("hasAuthority('PERM_APPROVE_PAYMENT')")
    public String markPaid(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Billing billing = billingService.markPaid(id, null);
            activityLogService.record("Billing", "APPROVE_PAYMENT",
                    "Marked bill " + billing.getInvoiceNumber() + " as paid", ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess",
                    "Bill " + billing.getInvoiceNumber() + " marked as paid");
        } catch (RuntimeException e) {
            activityLogService.record("Billing", "APPROVE_PAYMENT",
                    "Failed to mark bill #" + id + " as paid", ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/billings";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_DELETE_BILL')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Billing billing = billingService.findById(id);
            billingService.delete(id);
            activityLogService.record("Billing", "DELETE_BILL",
                    "Deleted bill " + billing.getInvoiceNumber(), ActivityStatus.SUCCESS);
            redirectAttributes.addFlashAttribute("flashSuccess", "Bill deleted successfully");
        } catch (RuntimeException e) {
            activityLogService.record("Billing", "DELETE_BILL",
                    "Failed to delete bill #" + id, ActivityStatus.FAILED);
            redirectAttributes.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/billings";
    }

    private void populateFormOptions(Model model) {
        model.addAttribute("patients", patientRepo.findByIsActiveTrue());
        model.addAttribute("appointments",
                appointmentRepo.findAll(Sort.by(Sort.Direction.DESC, "appointmentDate")));
        model.addAttribute("billStatuses",
                java.util.List.of("PENDING", "PAID", "PARTIALLY_PAID", "CANCELLED", "REFUNDED"));
    }
}
