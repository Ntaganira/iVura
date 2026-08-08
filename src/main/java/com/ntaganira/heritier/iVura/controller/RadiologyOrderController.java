package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.entity.RadiologyOrder;
import com.ntaganira.heritier.iVura.entity.User;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import com.ntaganira.heritier.iVura.repository.UserRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.RadiologyExamService;
import com.ntaganira.heritier.iVura.service.RadiologyOrderService;
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
 * - File      : RadiologyOrderController.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Radiology Order Controller
 * </pre>
 */
@Controller
@RequestMapping("/radiology-orders")
public class RadiologyOrderController {

    private final RadiologyOrderService orderService;
    private final RadiologyExamService examService;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final UserRepository userRepo;
    private final ActivityLogService activityLogService;

    public RadiologyOrderController(RadiologyOrderService orderService,
                                    RadiologyExamService examService,
                                    PatientRepository patientRepo,
                                    DoctorRepository doctorRepo,
                                    UserRepository userRepo,
                                    ActivityLogService activityLogService) {
        this.orderService = orderService;
        this.examService = examService;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.userRepo = userRepo;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_RAD_ORDER')")
    public String list(@RequestParam(required = false) String status, Model model) {
        List<RadiologyOrder> orders = orderService.findAll(status);
        model.addAttribute("orders", orders);
        model.addAttribute("itemCounts", orderService.itemCounts(orders));
        model.addAttribute("status", status);
        return "radiology-orders/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_RAD_ORDER')")
    public String addForm(@RequestParam(required = false) Long patientId, Model model) {
        model.addAttribute("patients", patientRepo.findByIsActiveTrue());
        model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
        model.addAttribute("exams", examService.findActive());
        model.addAttribute("patientId", patientId);
        return "radiology-orders/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_RAD_ORDER')")
    public String add(@RequestParam Long patientId,
                      @RequestParam(required = false) Long doctorId,
                      @RequestParam(required = false) String priority,
                      @RequestParam(required = false) String notes,
                      @RequestParam(required = false) List<Long> exams,
                      RedirectAttributes ra) {
        RadiologyOrder order = orderService.create(patientId, doctorId, priority, notes, exams, currentUser());
        activityLogService.record("Radiology", "CREATE_RAD_ORDER",
                "Order " + order.getOrderNumber() + " (" + order.getAccessionNumber()
                        + ") for " + order.getPatient().getFullName(), ActivityStatus.SUCCESS);
        ra.addFlashAttribute("flashSuccess",
                "Order " + order.getOrderNumber() + " created - accession " + order.getAccessionNumber());
        return "redirect:/radiology-orders";
    }

    @GetMapping("/perform/{id}")
    @PreAuthorize("hasAuthority('PERM_PERFORM_RAD_EXAM')")
    public String perform(@PathVariable Long id, RedirectAttributes ra) {
        RadiologyOrder order = orderService.perform(id);
        ra.addFlashAttribute("flashSuccess", "Exams marked as performed for " + order.getAccessionNumber());
        return "redirect:/radiology-orders";
    }

    @GetMapping("/cancel/{id}")
    @PreAuthorize("hasAuthority('PERM_MANAGE_RAD_ORDER')")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        RadiologyOrder order = orderService.cancel(id);
        ra.addFlashAttribute("flashSuccess", "Order " + order.getAccessionNumber() + " cancelled");
        return "redirect:/radiology-orders";
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? userRepo.findByUsername(auth.getName()).orElse(null) : null;
    }
}
