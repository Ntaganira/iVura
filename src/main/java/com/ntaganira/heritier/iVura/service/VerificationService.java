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
 * - File      : VerificationService.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Lab Result Verification / Approval Service
 * </pre>
 */
@Service
public class VerificationService {

    private final LabResultRepository labResultRepo;
    private final ResultSignoffRepository signoffRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public VerificationService(LabResultRepository labResultRepo,
                               ResultSignoffRepository signoffRepo,
                               UserRepository userRepo,
                               NotificationService notificationService,
                               ActivityLogService activityLogService) {
        this.labResultRepo = labResultRepo;
        this.signoffRepo = signoffRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    public List<LabResult> worklist() {
        return labResultRepo.findBySourceAndStatusInAndPublishedAtIsNullOrderByPerformedAtAsc(
                "AUTO", List.of("PENDING", "ABNORMAL"));
    }

    public long pendingCount() {
        return labResultRepo.countBySourceAndPublishedAtIsNull("AUTO");
    }

    public LabResult findById(Long id) {
        return labResultRepo.findById(id).orElse(null);
    }

    public List<ResultSignoff> signoffsOf(LabResult result) {
        return signoffRepo.findByLabResultIdOrderByCreatedAtDesc(result.getId());
    }

    @Transactional
    public LabResult verify(Long id, User user) {
        LabResult result = findById(id);
        if (result != null && result.getPublishedAt() == null) {
            result.setVerifiedBy(user);
            result.setVerifiedAt(LocalDateTime.now());
            result = labResultRepo.save(result);
            signoff(result, ResultSignoff.ACTION_VERIFY, user, "Reviewed and verified");
            activityLogService.record("Laboratory", "VERIFY_LAB",
                    "Verified " + result.getTestName() + " for " + result.getPatient().getFullName(),
                    ActivityStatus.SUCCESS);
        }
        return result;
    }

    @Transactional
    public LabResult publish(Long id, User user) {
        LabResult result = findById(id);
        if (result == null || result.getPublishedAt() != null) {
            return result;
        }
        result.setStatus("COMPLETED");
        result.setPublishedBy(user);
        result.setPublishedAt(LocalDateTime.now());
        if (result.getVerifiedAt() == null) {
            result.setVerifiedBy(user);
            result.setVerifiedAt(LocalDateTime.now());
        }
        result = labResultRepo.save(result);
        signoff(result, ResultSignoff.ACTION_PUBLISH, user, "Published to patient record");
        activityLogService.record("Laboratory", "PUBLISH_LAB",
                "Published " + result.getTestName() + " for " + result.getPatient().getFullName(),
                ActivityStatus.SUCCESS);
        if (result.getFlag() != null && result.getFlag().startsWith("CRITICAL")) {
            notifyAdmins("Critical lab result",
                    "CRITICAL " + result.getTestName() + " for " + result.getPatient().getFullName()
                            + ": " + result.getResult());
        }
        return result;
    }

    @Transactional
    public LabResult hold(Long id, User user, String reason) {
        LabResult result = findById(id);
        if (result != null && result.getPublishedAt() == null) {
            signoff(result, ResultSignoff.ACTION_HOLD, user,
                    reason != null && !reason.isBlank() ? reason : "Held for correlation");
        }
        return result;
    }

    @Transactional
    public LabResult reject(Long id, User user, String reason) {
        LabResult result = findById(id);
        if (result != null && result.getPublishedAt() == null) {
            result.setStatus("CANCELLED");
            result = labResultRepo.save(result);
            signoff(result, ResultSignoff.ACTION_REJECT, user,
                    reason != null && !reason.isBlank() ? reason : "Rejected");
            activityLogService.record("Laboratory", "REJECT_LAB",
                    "Rejected " + result.getTestName() + " for " + result.getPatient().getFullName(),
                    ActivityStatus.SUCCESS);
        }
        return result;
    }

    @Transactional
    public LabResult override(Long id, String value, String reason, User user) {
        LabResult result = findById(id);
        if (result != null && result.getPublishedAt() == null) {
            result.setResult(value);
            result.setOverrideReason(reason);
            result.setSource("MIXED");
            result = labResultRepo.save(result);
            signoff(result, ResultSignoff.ACTION_OVERRIDE, user, reason);
            activityLogService.record("Laboratory", "OVERRIDE_LAB",
                    "Overrode " + result.getTestName() + " for " + result.getPatient().getFullName(),
                    ActivityStatus.SUCCESS);
        }
        return result;
    }

    private void signoff(LabResult result, String action, User user, String reason) {
        signoffRepo.save(ResultSignoff.builder()
                .labResult(result)
                .signoffUser(user)
                .action(action)
                .reason(reason)
                .build());
    }

    private void notifyAdmins(String title, String message) {
        for (User admin : userRepo.findByRoles_Code("ADMIN")) {
            notificationService.notifyUser(admin.getId(), title, message,
                    NotificationService.TYPE_SYSTEM);
        }
    }
}
