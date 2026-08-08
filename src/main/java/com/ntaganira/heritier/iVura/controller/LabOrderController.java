package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.entity.LabOrder;
import com.ntaganira.heritier.iVura.entity.User;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import com.ntaganira.heritier.iVura.repository.UserRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.LabCatalogService;
import com.ntaganira.heritier.iVura.service.LabOrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : LabOrderController.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Laboratory Order Controller
 * </pre>
 */
@Controller
@RequestMapping("/lab-orders")
public class LabOrderController {

    private final LabOrderService orderService;
    private final LabCatalogService catalogService;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final UserRepository userRepo;
    private final ActivityLogService activityLogService;

    public LabOrderController(LabOrderService orderService,
                              LabCatalogService catalogService,
                              PatientRepository patientRepo,
                              DoctorRepository doctorRepo,
                              UserRepository userRepo,
                              ActivityLogService activityLogService) {
        this.orderService = orderService;
        this.catalogService = catalogService;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.userRepo = userRepo;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_ORDER')")
    public String list(@RequestParam(required = false) String status, Model model) {
        List<LabOrder> orders = orderService.findAll(status);
        model.addAttribute("orders", orders);
        model.addAttribute("itemCounts", orderService.itemCounts(orders));
        model.addAttribute("status", status);
        return "lab-orders/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_ORDER')")
    public String addForm(@RequestParam(required = false) Long patientId, Model model) {
        model.addAttribute("patients", patientRepo.findByIsActiveTrue());
        model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
        model.addAttribute("tests", catalogService.findActive());
        model.addAttribute("patientId", patientId);
        return "lab-orders/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_ORDER')")
    public String add(@RequestParam Long patientId,
                      @RequestParam(required = false) Long doctorId,
                      @RequestParam(required = false) String priority,
                      @RequestParam(required = false) String specimenType,
                      @RequestParam(required = false) String notes,
                      @RequestParam(required = false) List<Long> tests,
                      RedirectAttributes ra) {
        LabOrder order = orderService.create(patientId, doctorId, priority, specimenType,
                notes, tests, currentUser());
        activityLogService.record("Lab Orders", "CREATE_ORDER",
                "Order " + order.getOrderNumber() + " (" + order.getAccessionNumber()
                        + ") for " + order.getPatient().getFullName(), ActivityStatus.SUCCESS);
        ra.addFlashAttribute("flashSuccess",
                "Order " + order.getOrderNumber() + " created - accession " + order.getAccessionNumber());
        return "redirect:/lab-orders";
    }

    @GetMapping("/receive/{id}")
    @PreAuthorize("hasAuthority('PERM_MANAGE_ORDER')")
    public String receive(@PathVariable Long id, RedirectAttributes ra) {
        LabOrder order = orderService.markSpecimenReceived(id);
        ra.addFlashAttribute("flashSuccess", "Specimen received for " + order.getAccessionNumber());
        return "redirect:/lab-orders";
    }

    @GetMapping("/dispatch/{id}")
    @PreAuthorize("hasAuthority('PERM_MANAGE_ORDER')")
    public String dispatch(@PathVariable Long id, RedirectAttributes ra) {
        LabOrder order = orderService.dispatch(id);
        ra.addFlashAttribute("flashSuccess", "Order " + order.getAccessionNumber() + " dispatched to analyzer");
        return "redirect:/lab-orders";
    }

    @GetMapping("/cancel/{id}")
    @PreAuthorize("hasAuthority('PERM_MANAGE_ORDER')")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        LabOrder order = orderService.cancel(id);
        ra.addFlashAttribute("flashSuccess", "Order " + order.getAccessionNumber() + " cancelled");
        return "redirect:/lab-orders";
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? userRepo.findByUsername(auth.getName()).orElse(null) : null;
    }
}
