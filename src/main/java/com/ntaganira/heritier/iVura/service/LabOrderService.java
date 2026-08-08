package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.entity.*;
import com.ntaganira.heritier.iVura.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.service
 * - File      : LabOrderService.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Laboratory Order Service
 * </pre>
 */
@Service
public class LabOrderService {

    private final LabOrderRepository orderRepo;
    private final LabOrderItemRepository itemRepo;
    private final LabTestCatalogRepository catalogRepo;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final InstrumentGateway instrumentGateway;

    public LabOrderService(LabOrderRepository orderRepo,
                           LabOrderItemRepository itemRepo,
                           LabTestCatalogRepository catalogRepo,
                           PatientRepository patientRepo,
                           DoctorRepository doctorRepo,
                           InstrumentGateway instrumentGateway) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
        this.catalogRepo = catalogRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.instrumentGateway = instrumentGateway;
    }

    public List<LabOrder> findAll(String status) {
        return (status != null && !status.isBlank())
                ? orderRepo.findByStatusOrderByRequestedAtDesc(status)
                : orderRepo.findAllByOrderByRequestedAtDesc();
    }

    public LabOrder findById(Long id) {
        return orderRepo.findById(id).orElse(null);
    }

    public List<LabOrderItem> itemsOf(LabOrder order) {
        return itemRepo.findByOrderIdOrderByIdAsc(order.getId());
    }

    public Map<Long, Long> itemCounts(List<LabOrder> orders) {
        return orders.stream().collect(java.util.stream.Collectors.toMap(
                LabOrder::getId, o -> itemRepo.countByOrderId(o.getId()), (a, b) -> a));
    }

    @Transactional
    public LabOrder create(Long patientId, Long doctorId, String priority, String specimenType,
                           String notes, List<Long> catalogIds, User orderedBy) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        LabOrder order = LabOrder.builder()
                .orderNumber(generateNumber("ORDER"))
                .accessionNumber(generateNumber("ACC"))
                .patient(patient)
                .doctor(doctorId != null ? doctorRepo.findById(doctorId).orElse(null) : null)
                .priority(priority != null && !priority.isBlank() ? priority.toUpperCase() : "ROUTINE")
                .specimenType(specimenType)
                .orderedBy(orderedBy)
                .notes(notes)
                .build();
        order = orderRepo.save(order);

        if (catalogIds != null) {
            for (Long catalogId : catalogIds) {
                LabTestCatalog catalog = catalogRepo.findById(catalogId).orElse(null);
                if (catalog == null) {
                    continue;
                }
                itemRepo.save(LabOrderItem.builder()
                        .order(order)
                        .catalog(catalog)
                        .testName(catalog.getName())
                        .build());
            }
        }
        return order;
    }

    @Transactional
    public LabOrder markSpecimenReceived(Long id) {
        LabOrder order = findById(id);
        if (order != null && !LabOrder.STATUS_CANCELLED.equals(order.getStatus())) {
            order.setStatus(LabOrder.STATUS_SPECIMEN_RECEIVED);
            order.setSpecimenReceivedAt(LocalDateTime.now());
            order = orderRepo.save(order);
        }
        return order;
    }

    @Transactional
    public LabOrder dispatch(Long id) {
        LabOrder order = findById(id);
        if (order != null && !LabOrder.STATUS_CANCELLED.equals(order.getStatus())) {
            order.setStatus(LabOrder.STATUS_IN_PROGRESS);
            order = orderRepo.save(order);
            instrumentGateway.sendWorkOrder(order, itemsOf(order));
        }
        return order;
    }

    @Transactional
    public LabOrder cancel(Long id) {
        LabOrder order = findById(id);
        if (order != null) {
            order.setStatus(LabOrder.STATUS_CANCELLED);
            order = orderRepo.save(order);
        }
        return order;
    }

    @Transactional
    public LabOrder completeOrder(Long id) {
        LabOrder order = findById(id);
        if (order != null && !LabOrder.STATUS_CANCELLED.equals(order.getStatus())) {
            order.setStatus(LabOrder.STATUS_COMPLETED);
            order = orderRepo.save(order);
        }
        return order;
    }

    public void linkResult(LabOrderItem item, LabResult result) {
        item.setResult(result);
        item.setStatus(LabOrderItem.STATUS_RESULT_RECEIVED);
        itemRepo.save(item);
    }

    private String generateNumber(String prefix) {
        String yearPrefix = prefix + "-" + Year.now() + "-";
        long seq = 1;
        for (LabOrder order : orderRepo.findAll()) {
            String number = prefix.equals("ORDER") ? order.getOrderNumber() : order.getAccessionNumber();
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
