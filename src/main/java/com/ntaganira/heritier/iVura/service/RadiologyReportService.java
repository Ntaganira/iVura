package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.entity.*;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.service
 * - File      : RadiologyReportService.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Radiology Reporting / Verification Service
 * </pre>
 */
@Service
public class RadiologyReportService {

    private final RadiologyReportRepository reportRepo;
    private final RadiologyOrderItemRepository itemRepo;
    private final RadiologyOrderService orderService;
    private final ActivityLogService activityLogService;

    public RadiologyReportService(RadiologyReportRepository reportRepo,
                                  RadiologyOrderItemRepository itemRepo,
                                  RadiologyOrderService orderService,
                                  ActivityLogService activityLogService) {
        this.reportRepo = reportRepo;
        this.itemRepo = itemRepo;
        this.orderService = orderService;
        this.activityLogService = activityLogService;
    }

    public List<RadiologyOrderItem> toReport() {
        return itemRepo.findByStatusOrderByIdAsc(RadiologyOrderItem.STATUS_IMAGED);
    }

    public List<RadiologyReport> toVerify() {
        return reportRepo.findByStatusOrderByCreatedAtAsc(RadiologyReport.STATUS_FINAL);
    }

    public RadiologyReport findById(Long id) {
        return reportRepo.findById(id).orElse(null);
    }

    public RadiologyReport findByItem(Long itemId) {
        return reportRepo.findByOrderItemId(itemId).orElse(null);
    }

    @Transactional
    public RadiologyReport saveDraft(Long itemId, String clinicalHistory, String findings,
                                     String impression, User reporter) {
        RadiologyOrderItem item = itemRepo.findById(itemId).orElse(null);
        if (item == null) {
            return null;
        }
        RadiologyReport report = findByItem(itemId);
        if (report == null) {
            report = RadiologyReport.builder()
                    .orderItem(item)
                    .reportedBy(reporter)
                    .reportedAt(LocalDateTime.now())
                    .build();
        }
        report.setClinicalHistory(clinicalHistory);
        report.setFindings(findings);
        report.setImpression(impression);
        report.setStatus(RadiologyReport.STATUS_DRAFT);
        return reportRepo.save(report);
    }

    @Transactional
    public RadiologyReport submit(Long itemId, String clinicalHistory, String findings,
                                  String impression, User reporter) {
        RadiologyOrderItem item = itemRepo.findById(itemId).orElse(null);
        if (item == null) {
            return null;
        }
        RadiologyReport report = saveDraft(itemId, clinicalHistory, findings, impression, reporter);
        report.setStatus(RadiologyReport.STATUS_FINAL);
        if (report.getReportedAt() == null) {
            report.setReportedAt(LocalDateTime.now());
        }
        if (report.getReportedBy() == null) {
            report.setReportedBy(reporter);
        }
        report = reportRepo.save(report);

        item.setStatus(RadiologyOrderItem.STATUS_REPORTED);
        itemRepo.save(item);

        activityLogService.record("Radiology", "WRITE_RAD_REPORT",
                "Reported " + item.getExamName() + " for " + item.getOrder().getPatient().getFullName(),
                ActivityStatus.SUCCESS);
        return report;
    }

    @Transactional
    public RadiologyReport verify(Long id, User verifier) {
        RadiologyReport report = findById(id);
        if (report == null || RadiologyReport.STATUS_VERIFIED.equals(report.getStatus())) {
            return report;
        }
        report.setStatus(RadiologyReport.STATUS_VERIFIED);
        report.setVerifiedBy(verifier);
        report.setVerifiedAt(LocalDateTime.now());
        report = reportRepo.save(report);

        RadiologyOrderItem item = report.getOrderItem();
        item.setStatus(RadiologyOrderItem.STATUS_VERIFIED);
        itemRepo.save(item);

        orderService.completeIfAllVerified(item.getOrder());

        activityLogService.record("Radiology", "VERIFY_RAD_REPORT",
                "Verified " + item.getExamName() + " report for " + item.getOrder().getPatient().getFullName(),
                ActivityStatus.SUCCESS);
        return report;
    }

    @Transactional
    public RadiologyReport reopen(Long id, User user) {
        RadiologyReport report = findById(id);
        if (report == null) {
            return null;
        }
        report.setStatus(RadiologyReport.STATUS_DRAFT);
        report.setVerifiedBy(null);
        report.setVerifiedAt(null);
        report = reportRepo.save(report);

        RadiologyOrderItem item = report.getOrderItem();
        if (RadiologyOrderItem.STATUS_VERIFIED.equals(item.getStatus())) {
            item.setStatus(RadiologyOrderItem.STATUS_IMAGED);
            itemRepo.save(item);
        }
        return report;
    }
}
