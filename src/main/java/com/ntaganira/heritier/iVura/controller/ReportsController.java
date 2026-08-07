package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.ReportData;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.PdfService;
import com.ntaganira.heritier.iVura.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    private final ReportService reportService;
    private final PdfService pdfService;
    private final ActivityLogService activityLogService;

    public ReportsController(ReportService reportService, PdfService pdfService,
                             ActivityLogService activityLogService) {
        this.reportService = reportService;
        this.pdfService = pdfService;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_REPORT')")
    public String index(@RequestParam(required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false)
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                        Model model) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusDays(29);
        model.addAttribute("report", reportService.buildReport(start, end));
        model.addAttribute("from", start);
        model.addAttribute("to", end);
        return "reports/index";
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('PERM_EXPORT_REPORT')")
    public ResponseEntity<byte[]> export(@RequestParam(required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        LocalDate end = to != null ? to : LocalDate.now();
        LocalDate start = from != null ? from : end.minusDays(29);
        ReportData report = reportService.buildReport(start, end);

        Map<String, Object> model = new HashMap<>();
        model.put("report", report);
        model.put("from", start);
        model.put("to", end);
        byte[] pdf = pdfService.renderPdf("reports/pdf", model);

        activityLogService.record("Reports", "EXPORT_REPORT",
                "Exported PDF report for " + start + " to " + end, ActivityStatus.SUCCESS);

        String filename = "report-" + start + "_to_" + end + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
