package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.entity.RadiologyOrderItem;
import com.ntaganira.heritier.iVura.entity.RadiologyReport;
import com.ntaganira.heritier.iVura.entity.User;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.UserRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.PdfService;
import com.ntaganira.heritier.iVura.service.RadiologyReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : RadiologyWorkbenchController.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Radiology Reporting / Verification Workbench Controller
 * </pre>
 */
@Controller
@RequestMapping("/radiology-workbench")
public class RadiologyWorkbenchController {

    private final RadiologyReportService reportService;
    private final UserRepository userRepo;
    private final PdfService pdfService;
    private final ActivityLogService activityLogService;

    public RadiologyWorkbenchController(RadiologyReportService reportService,
                                        UserRepository userRepo,
                                        PdfService pdfService,
                                        ActivityLogService activityLogService) {
        this.reportService = reportService;
        this.userRepo = userRepo;
        this.pdfService = pdfService;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PERM_WRITE_RAD_REPORT', 'PERM_VERIFY_RAD_REPORT')")
    public String worklist(Model model) {
        model.addAttribute("toReport", reportService.toReport());
        model.addAttribute("toVerify", reportService.toVerify());
        return "radiology-workbench/list";
    }

    @GetMapping("/report/{itemId}")
    @PreAuthorize("hasAuthority('PERM_WRITE_RAD_REPORT')")
    public String reportForm(@PathVariable Long itemId, Model model) {
        RadiologyReport existing = reportService.findByItem(itemId);
        RadiologyOrderItem item = existing != null ? existing.getOrderItem() : null;
        if (item == null) {
            for (RadiologyOrderItem candidate : reportService.toReport()) {
                if (candidate.getId().equals(itemId)) {
                    item = candidate;
                    break;
                }
            }
        }
        if (item == null) {
            return "redirect:/radiology-workbench";
        }
        model.addAttribute("item", item);
        model.addAttribute("report", existing);
        return "radiology-workbench/report";
    }

    @PostMapping("/save/{itemId}")
    @PreAuthorize("hasAuthority('PERM_WRITE_RAD_REPORT')")
    public String saveDraft(@PathVariable Long itemId,
                            @RequestParam(required = false) String clinicalHistory,
                            @RequestParam(required = false) String findings,
                            @RequestParam(required = false) String impression,
                            RedirectAttributes ra) {
        reportService.saveDraft(itemId, clinicalHistory, findings, impression, currentUser());
        ra.addFlashAttribute("flashSuccess", "Draft saved");
        return "redirect:/radiology-workbench/report/" + itemId;
    }

    @PostMapping("/submit/{itemId}")
    @PreAuthorize("hasAuthority('PERM_WRITE_RAD_REPORT')")
    public String submit(@PathVariable Long itemId,
                         @RequestParam(required = false) String clinicalHistory,
                         @RequestParam(required = false) String findings,
                         @RequestParam(required = false) String impression,
                         RedirectAttributes ra) {
        RadiologyReport report = reportService.submit(itemId, clinicalHistory, findings, impression, currentUser());
        ra.addFlashAttribute("flashSuccess", report != null ? "Report submitted for verification" : "Item not found");
        return "redirect:/radiology-workbench";
    }

    @PostMapping("/verify/{id}")
    @PreAuthorize("hasAuthority('PERM_VERIFY_RAD_REPORT')")
    public String verify(@PathVariable Long id, RedirectAttributes ra) {
        RadiologyReport report = reportService.verify(id, currentUser());
        ra.addFlashAttribute("flashSuccess", "Report verified and signed off");
        return "redirect:/radiology-workbench";
    }

    @PostMapping("/reopen/{id}")
    @PreAuthorize("hasAuthority('PERM_VERIFY_RAD_REPORT')")
    public String reopen(@PathVariable Long id, RedirectAttributes ra) {
        RadiologyReport report = reportService.reopen(id, currentUser());
        ra.addFlashAttribute("flashSuccess", "Report reopened for revision");
        return "redirect:/radiology-workbench";
    }

    @GetMapping("/print/{id}")
    @PreAuthorize("hasAuthority('PERM_PRINT_RAD_REPORT')")
    public ResponseEntity<byte[]> print(@PathVariable Long id) {
        RadiologyReport report = reportService.findById(id);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> model = new HashMap<>();
        model.put("report", report);
        byte[] pdf = pdfService.renderPdf("radiology-workbench/report-pdf", model);

        String patientName = report.getOrderItem().getOrder().getPatient().getFullName();
        String accession = report.getOrderItem().getOrder().getAccessionNumber();
        activityLogService.record("Radiology", "PRINT_RAD_REPORT",
                "Printed " + report.getOrderItem().getExamName() + " report for " + patientName,
                ActivityStatus.SUCCESS);

        String filename = "radiology-report-" + accession + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? userRepo.findByUsername(auth.getName()).orElse(null) : null;
    }
}
