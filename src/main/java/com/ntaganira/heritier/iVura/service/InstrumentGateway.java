package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.entity.*;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.service
 * - File      : InstrumentGateway.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Instrument integration gateway (CSV file-drop adapter)
 * </pre>
 *
 * <p>Inbound CSV contract (one row per analyte):
 * <pre>message_id,accession_number,device_code,test_code,result_value,unit,flag,performed_at</pre>
 * Outbound work orders: {@code accession_number,test_code} per ordered test.
 */
@Service
public class InstrumentGateway {

    private static final Logger log = LoggerFactory.getLogger(InstrumentGateway.class);

    private final LabOrderRepository orderRepo;
    private final LabOrderItemRepository itemRepo;
    private final LabResultRepository labResultRepo;
    private final AnalyzerDeviceRepository deviceRepo;
    private final AnalyzerTestMapRepository testMapRepo;
    private final InstrumentMessageRepository messageRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    @Value("${lab-integration.inbox:C:/ivura-lab/inbox}")
    private String inboxDir;

    @Value("${lab-integration.outbox:C:/ivura-lab/outbox}")
    private String outboxDir;

    public InstrumentGateway(LabOrderRepository orderRepo,
                             LabOrderItemRepository itemRepo,
                             LabResultRepository labResultRepo,
                             AnalyzerDeviceRepository deviceRepo,
                             AnalyzerTestMapRepository testMapRepo,
                             InstrumentMessageRepository messageRepo,
                             UserRepository userRepo,
                             NotificationService notificationService,
                             ActivityLogService activityLogService) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
        this.labResultRepo = labResultRepo;
        this.deviceRepo = deviceRepo;
        this.testMapRepo = testMapRepo;
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    // ------------------------------------------------------------------
    // Outbound: send a work order to the analyzer (file drop)
    // ------------------------------------------------------------------
    public void sendWorkOrder(LabOrder order, List<LabOrderItem> items) {
        try {
            Path out = Paths.get(outboxDir);
            Files.createDirectories(out);
            Path file = out.resolve("order-" + order.getAccessionNumber() + ".csv");

            AnalyzerDevice device = deviceRepo.findByIsActiveTrueOrderByNameAsc()
                    .stream().findFirst().orElse(null);

            StringBuilder sb = new StringBuilder("accession_number,test_code\n");
            for (LabOrderItem item : items) {
                String testCode = item.getCatalog() != null ? item.getCatalog().getCode() : item.getTestName();
                if (device != null && item.getCatalog() != null) {
                    String mapped = testMapRepo.findByDeviceIdAndDeviceTestCode(device.getId(), testCode)
                            .map(AnalyzerTestMap::getDeviceTestCode).orElse(null);
                    if (mapped != null) {
                        testCode = mapped;
                    }
                }
                sb.append(order.getAccessionNumber()).append(',').append(testCode).append('\n');
            }
            Files.write(file, sb.toString().getBytes(StandardCharsets.UTF_8));

            saveMessage(order, device, InstrumentMessage.DIR_OUTBOUND,
                    InstrumentMessage.STATUS_ACCEPTED, null, null,
                    "Work order dispatched to outbox: " + file);
            log.info("Work order written to {}", file);
        } catch (IOException e) {
            log.error("Failed to write work order for {}", order.getAccessionNumber(), e);
            saveMessage(order, null, InstrumentMessage.DIR_OUTBOUND,
                    InstrumentMessage.STATUS_ERROR, "OUTBOUND_IO", e.getMessage(), null);
        }
    }

    // ------------------------------------------------------------------
    // Inbound: import result files from the inbox
    // ------------------------------------------------------------------
    @Transactional
    public int importInbox() {
        int imported = 0;
        Path inbox = Paths.get(inboxDir);
        try {
            Files.createDirectories(inbox);
            Files.createDirectories(inbox.resolve("processed"));

            List<Path> files;
            try (var stream = Files.list(inbox)) {
                files = stream.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".csv"))
                        .toList();
            }
            for (Path file : files) {
                imported += processFile(file);
            }
        } catch (IOException e) {
            log.error("Inbox scan failed", e);
        }
        if (imported > 0) {
            activityLogService.record("Lab Integration", "LAB_IMPORT",
                    "Imported " + imported + " analyzer result row(s)", ActivityStatus.SUCCESS);
        }
        return imported;
    }

    /**
     * Re-process a quarantined message straight from its stored payload
     * (used by the retry action in the integration console).
     */
    @Transactional
    public void reprocess(Long messageId) {
        InstrumentMessage message = messageRepo.findById(messageId).orElse(null);
        if (message == null || message.getPayload() == null) {
            return;
        }
        processRow(message.getPayload().trim(), "retry-of-message-" + messageId);
    }

    private int processFile(Path file) {
        int processed = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            boolean header = line != null && line.toLowerCase().startsWith("message_id");
            if (header) {
                line = reader.readLine();
            }
            while (line != null) {
                if (!line.isBlank()) {
                    processRow(line.trim(), file.getFileName().toString());
                    processed++;
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            saveError(InstrumentMessage.STATUS_ERROR, "INBOX_IO",
                    "Failed reading " + file.getFileName() + ": " + e.getMessage(),
                    null, null);
            return 0;
        }
        try {
            Files.move(file, file.resolveSibling("processed").resolve(file.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.warn("Could not move processed file {}", file, e);
        }
        return processed;
    }

    private void processRow(String row, String sourceFile) {
        String[] cols = splitCsv(row);
        String messageId = col(cols, 0);
        String accession = col(cols, 1);
        String deviceCode = col(cols, 2);
        String testCode = col(cols, 3);
        String value = col(cols, 4);
        String unit = col(cols, 5);
        String performedAt = col(cols, 7);

        // 1. Idempotency: reuse an existing non-accepted message so retries
        //    update the same record instead of duplicating it.
        Optional<InstrumentMessage> existing = messageRepo.findByMessageId(messageId);
        if (existing.isPresent() && InstrumentMessage.STATUS_ACCEPTED.equals(existing.get().getStatus())) {
            saveError(InstrumentMessage.STATUS_DUPLICATE, "DUPLICATE_MESSAGE",
                    "Message " + messageId + " already processed", accession, deviceCode);
            return;
        }
        InstrumentMessage message = existing.orElseGet(() -> messageRepo.save(InstrumentMessage.builder()
                .messageId(messageId)
                .direction(InstrumentMessage.DIR_INBOUND)
                .status(InstrumentMessage.STATUS_RECEIVED)
                .build()));

        // 2. Required-field validation (incomplete payload)
        if (!hasRequired(messageId, accession, testCode, value)) {
            fail(message, InstrumentMessage.STATUS_INCOMPLETE, "INCOMPLETE_ROW",
                    "Missing required field(s) in row: " + row, accession, deviceCode);
            return;
        }
        // 3. Patient / accession resolution
        LabOrder order = orderRepo.findByAccessionNumber(accession).orElse(null);
        if (order == null) {
            fail(message, InstrumentMessage.STATUS_UNMATCHED, "UNKNOWN_ACCESSION",
                    "No lab order found for accession " + accession + " (" + sourceFile + ")",
                    accession, deviceCode);
            notifyAdmins("Unmatched lab result",
                    "Result for accession " + accession + " could not be matched to a patient. See Lab Integration console.");
            return;
        }
        // 4. Device + test mapping resolution
        AnalyzerDevice device = resolveDevice(deviceCode);
        LabTestCatalog catalog = resolveCatalog(device, testCode);
        if (catalog == null) {
            fail(message, InstrumentMessage.STATUS_ERROR, "UNMAPPED_TEST",
                    "No catalog mapping for device test code '" + testCode + "'", accession, deviceCode);
            return;
        }
        // 5. Persist the AUTO result (draft, awaiting verification)
        message.setDevice(device);
        message.setAccessionNumber(accession);
        message.setPatientRef(order.getPatient().getFullName());
        message.setPayload(row);
        message.setStatus(InstrumentMessage.STATUS_ACCEPTED);
        message.setErrorCode(null);
        message.setErrorDetail("Accepted from " + sourceFile);
        message.setProcessedAt(LocalDateTime.now());
        messageRepo.save(message);

        LabResult result = LabResult.builder()
                .patient(order.getPatient())
                .doctor(order.getDoctor())
                .testName(catalog.getName())
                .category(catalog.getCategory())
                .result(value)
                .unit(unit != null && !unit.isBlank() ? unit : catalog.getUnit())
                .normalRange(catalog.getNormalRange())
                .status("PENDING")
                .source("AUTO")
                .accessionNumber(accession)
                .device(device)
                .instrumentMessage(message)
                .flag(computeFlag(catalog, value))
                .performedAt(parseDateTime(performedAt))
                .build();
        labResultRepo.save(result);

        Optional<LabOrderItem> item = itemRepo.findByOrderIdOrderByIdAsc(order.getId())
                .stream().filter(i -> i.getCatalog() != null && i.getCatalog().getId().equals(catalog.getId()))
                .findFirst();
        item.ifPresent(i -> {
            i.setResult(result);
            i.setStatus(LabOrderItem.STATUS_RESULT_RECEIVED);
            itemRepo.save(i);
        });

        boolean allReceived = itemRepo.countByOrderIdAndStatus(order.getId(), LabOrderItem.STATUS_RESULT_RECEIVED)
                >= itemRepo.findByOrderIdOrderByIdAsc(order.getId()).size();
        if (allReceived && !LabOrder.STATUS_COMPLETED.equals(order.getStatus())) {
            order.setStatus(LabOrder.STATUS_COMPLETED);
            orderRepo.save(order);
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    private void fail(InstrumentMessage message, String status, String errorCode, String detail,
                      String accession, String deviceCode) {
        message.setStatus(status);
        message.setErrorCode(errorCode);
        message.setErrorDetail(detail);
        message.setAccessionNumber(accession);
        message.setPayload(message.getPayload() != null ? message.getPayload() : "");
        message.setProcessedAt(LocalDateTime.now());
        messageRepo.save(message);
        activityLogService.record("Lab Integration", "LAB_IMPORT",
                "Quarantined message " + message.getId() + " [" + errorCode + "]: " + detail,
                ActivityStatus.FAILED);
    }

    private void saveError(String status, String errorCode, String detail,
                           String accession, String deviceCode) {
        messageRepo.save(InstrumentMessage.builder()
                .messageId("ROW-" + System.nanoTime() + "-" + (accession != null ? accession : "x"))
                .direction(InstrumentMessage.DIR_INBOUND)
                .status(status)
                .accessionNumber(accession)
                .errorCode(errorCode)
                .errorDetail(detail)
                .processedAt(LocalDateTime.now())
                .build());
    }

    private AnalyzerDevice resolveDevice(String deviceCode) {
        if (deviceCode != null && !deviceCode.isBlank()) {
            return deviceRepo.findByCode(deviceCode).orElse(null);
        }
        return deviceRepo.findByIsActiveTrueOrderByNameAsc().stream().findFirst().orElse(null);
    }

    private LabTestCatalog resolveCatalog(AnalyzerDevice device, String testCode) {
        if (device == null) {
            return null;
        }
        return testMapRepo.findByDeviceIdAndDeviceTestCode(device.getId(), testCode)
                .map(AnalyzerTestMap::getCatalog).orElse(null);
    }

    private String computeFlag(LabTestCatalog c, String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            BigDecimal v = new BigDecimal(raw.trim());
            if (c.getCriticalLow() != null && v.compareTo(c.getCriticalLow()) < 0) {
                return "CRITICAL_LOW";
            }
            if (c.getCriticalHigh() != null && v.compareTo(c.getCriticalHigh()) > 0) {
                return "CRITICAL_HIGH";
            }
            if (c.getRefLow() != null && v.compareTo(c.getRefLow()) < 0) {
                return "LOW";
            }
            if (c.getRefHigh() != null && v.compareTo(c.getRefHigh()) > 0) {
                return "HIGH";
            }
            return "NORMAL";
        } catch (NumberFormatException e) {
            return "ABNORMAL";
        }
    }

    private LocalDateTime parseDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(raw);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private InstrumentMessage saveMessage(LabOrder order, AnalyzerDevice device, String direction,
                                          String status, String accession, String deviceCode, String detail) {
        InstrumentMessage message = InstrumentMessage.builder()
                .messageId(direction + "-" + (order != null ? order.getAccessionNumber() : "n/a")
                        + "-" + System.nanoTime())
                .device(device)
                .direction(direction)
                .status(status)
                .accessionNumber(accession)
                .patientRef(order != null ? order.getPatient().getFullName() : null)
                .errorDetail(detail)
                .processedAt(LocalDateTime.now())
                .build();
        return messageRepo.save(message);
    }

    private void notifyAdmins(String title, String message) {
        for (User admin : userRepo.findByRoles_Code("ADMIN")) {
            notificationService.notifyUser(admin.getId(), title, message,
                    NotificationService.TYPE_SYSTEM);
        }
    }

    private boolean hasRequired(String... values) {
        for (String v : values) {
            if (v == null || v.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String[] splitCsv(String row) {
        // simple comma split (device payloads use commas only as separators)
        return row.split(",", -1);
    }

    private String col(String[] cols, int i) {
        return i < cols.length ? cols[i].trim() : null;
    }
}
