package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.RadiologyHistoryDto;
import com.ntaganira.heritier.iVura.entity.*;
import com.ntaganira.heritier.iVura.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.service
 * - File      : RadiologyOrderService.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Radiology Order Service
 * </pre>
 */
@Service
public class RadiologyOrderService {

    private final RadiologyOrderRepository orderRepo;
    private final RadiologyOrderItemRepository itemRepo;
    private final RadiologyExamRepository examRepo;
    private final RadiologyReportRepository reportRepo;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;

    public RadiologyOrderService(RadiologyOrderRepository orderRepo,
                                 RadiologyOrderItemRepository itemRepo,
                                 RadiologyExamRepository examRepo,
                                 RadiologyReportRepository reportRepo,
                                 PatientRepository patientRepo,
                                 DoctorRepository doctorRepo) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
        this.examRepo = examRepo;
        this.reportRepo = reportRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
    }

    public List<RadiologyOrder> findAll(String status) {
        return (status != null && !status.isBlank())
                ? orderRepo.findByStatusOrderByRequestedAtDesc(status)
                : orderRepo.findAllByOrderByRequestedAtDesc();
    }

    public RadiologyOrder findById(Long id) {
        return orderRepo.findById(id).orElse(null);
    }

    public List<RadiologyOrderItem> itemsOf(RadiologyOrder order) {
        return itemRepo.findByOrderIdOrderByIdAsc(order.getId());
    }

    public Map<Long, Long> itemCounts(List<RadiologyOrder> orders) {
        return orders.stream().collect(java.util.stream.Collectors.toMap(
                RadiologyOrder::getId, o -> itemRepo.countByOrderId(o.getId()), (a, b) -> a));
    }

    public List<RadiologyOrder> findByPatientId(Long patientId) {
        return orderRepo.findByPatientIdOrderByRequestedAtDesc(patientId);
    }

    public List<RadiologyHistoryDto> historyForPatient(Long patientId) {
        return orderRepo.findByPatientIdOrderByRequestedAtDesc(patientId).stream()
                .map(order -> {
                    List<RadiologyOrderItem> items = itemsOf(order);
                    RadiologyHistoryDto dto = new RadiologyHistoryDto();
                    dto.setOrder(order);
                    dto.setItems(items.stream()
                            .map(item -> {
                                RadiologyReport report = reportRepo.findByOrderItemId(item.getId()).orElse(null);
                                return new RadiologyHistoryDto.Item(item, report);
                            })
                            .collect(Collectors.toList()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public long performedCount(RadiologyOrder order) {
        return itemRepo.countByOrderIdAndStatus(order.getId(), RadiologyOrderItem.STATUS_IMAGED);
    }

    public long reportedCount(RadiologyOrder order) {
        return itemRepo.countByOrderIdAndStatus(order.getId(), RadiologyOrderItem.STATUS_REPORTED);
    }

    public long verifiedCount(RadiologyOrder order) {
        return itemRepo.countByOrderIdAndStatus(order.getId(), RadiologyOrderItem.STATUS_VERIFIED);
    }

    @Transactional
    public RadiologyOrder create(Long patientId, Long doctorId, String priority,
                                 String notes, List<Long> examIds, User orderedBy) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        RadiologyOrder order = RadiologyOrder.builder()
                .orderNumber(generateNumber("RAD"))
                .accessionNumber(generateNumber("IMAG"))
                .patient(patient)
                .doctor(doctorId != null ? doctorRepo.findById(doctorId).orElse(null) : null)
                .priority(priority != null && !priority.isBlank() ? priority.toUpperCase() : "ROUTINE")
                .orderedBy(orderedBy)
                .notes(notes)
                .build();
        order = orderRepo.save(order);

        if (examIds != null) {
            for (Long examId : examIds) {
                RadiologyExam exam = examRepo.findById(examId).orElse(null);
                if (exam == null) {
                    continue;
                }
                itemRepo.save(RadiologyOrderItem.builder()
                        .order(order)
                        .exam(exam)
                        .examName(exam.getName())
                        .build());
            }
        }
        return order;
    }

    @Transactional
    public RadiologyOrder perform(Long id) {
        RadiologyOrder order = findById(id);
        if (order != null && !RadiologyOrder.STATUS_CANCELLED.equals(order.getStatus())) {
            for (RadiologyOrderItem item : itemsOf(order)) {
                if (RadiologyOrderItem.STATUS_ORDERED.equals(item.getStatus())) {
                    item.setStatus(RadiologyOrderItem.STATUS_IMAGED);
                    itemRepo.save(item);
                }
            }
            order.setStatus(RadiologyOrder.STATUS_IN_PROGRESS);
            order = orderRepo.save(order);
        }
        return order;
    }

    @Transactional
    public RadiologyOrder cancel(Long id) {
        RadiologyOrder order = findById(id);
        if (order != null && !RadiologyOrder.STATUS_COMPLETED.equals(order.getStatus())) {
            order.setStatus(RadiologyOrder.STATUS_CANCELLED);
            order = orderRepo.save(order);
        }
        return order;
    }

    @Transactional
    public void completeIfAllVerified(RadiologyOrder order) {
        long total = itemRepo.countByOrderId(order.getId());
        long verified = itemRepo.countByOrderIdAndStatus(order.getId(), RadiologyOrderItem.STATUS_VERIFIED);
        if (total > 0 && total == verified
                && !RadiologyOrder.STATUS_CANCELLED.equals(order.getStatus())) {
            order.setStatus(RadiologyOrder.STATUS_COMPLETED);
            orderRepo.save(order);
        }
    }

    private String generateNumber(String prefix) {
        String yearPrefix = prefix + "-" + Year.now() + "-";
        long seq = 1;
        for (RadiologyOrder order : orderRepo.findAll()) {
            String number = prefix.equals("RAD") ? order.getOrderNumber() : order.getAccessionNumber();
            if (number != null && number.startsWith(yearPrefix)) {
                try {
                    long candidate = Long.parseLong(number.substring(yearPrefix.length())) + 1;
                    if (candidate > seq) {
                        seq = candidate;
                    }
                } catch (NumberFormatException ignored) {
                    // skip non-conforming numbers
                }
            }
        }
        return String.format("%s%06d", yearPrefix, seq);
    }
}
