package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.entity.Dispensation;
import com.ntaganira.heritier.iVura.entity.Medicine;
import com.ntaganira.heritier.iVura.entity.Patient;
import com.ntaganira.heritier.iVura.entity.User;
import com.ntaganira.heritier.iVura.repository.DispensationRepository;
import com.ntaganira.heritier.iVura.repository.MedicineRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.service
 * - File      : PharmacyService.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Pharmacy medicine inventory and dispensing service
 * </pre>
 */
@Service
public class PharmacyService {

    private final MedicineRepository medicineRepo;
    private final DispensationRepository dispensationRepo;
    private final PatientRepository patientRepo;

    public PharmacyService(MedicineRepository medicineRepo,
                           DispensationRepository dispensationRepo,
                           PatientRepository patientRepo) {
        this.medicineRepo = medicineRepo;
        this.dispensationRepo = dispensationRepo;
        this.patientRepo = patientRepo;
    }

    public List<Medicine> findAllMedicines() {
        return medicineRepo.findByIsActiveTrueOrderByNameAsc();
    }

    public List<Medicine> lowStockMedicines() {
        return medicineRepo.findAll().stream()
                .filter(Medicine::isLowStock)
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
    }

    public Medicine findMedicine(Long id) {
        return medicineRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));
    }

    @Transactional
    public Medicine saveMedicine(Medicine medicine, Long id) {
        if (id != null) {
            Medicine existing = findMedicine(id);
            medicine.setId(id);
            medicine.setCreatedAt(existing.getCreatedAt());
            medicine.setIsActive(existing.getIsActive() != null ? existing.getIsActive() : true);
        }
        if (medicine.getIsActive() == null) {
            medicine.setIsActive(true);
        }
        if (medicine.getStockQuantity() == null) {
            medicine.setStockQuantity(0);
        }
        return medicineRepo.save(medicine);
    }

    @Transactional
    public Medicine adjustStock(Long id, int adjustment) {
        Medicine medicine = findMedicine(id);
        int current = medicine.getStockQuantity() != null ? medicine.getStockQuantity() : 0;
        int next = Math.max(0, current + adjustment);
        medicine.setStockQuantity(next);
        return medicineRepo.save(medicine);
    }

    @Transactional
    public Dispensation dispense(Long patientId, Long medicineId, int quantity, String note, User dispensedBy) {
        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than zero");
        }
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Medicine medicine = findMedicine(medicineId);
        int stock = medicine.getStockQuantity() != null ? medicine.getStockQuantity() : 0;
        if (stock < quantity) {
            throw new RuntimeException("Insufficient stock: only " + stock + " of "
                    + medicine.getName() + " available");
        }
        BigDecimal unitPrice = medicine.getUnitPrice() != null ? medicine.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);

        medicine.setStockQuantity(stock - quantity);
        medicineRepo.save(medicine);

        Dispensation dispensation = Dispensation.builder()
                .patient(patient)
                .medicine(medicine)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .total(total)
                .dispensedBy(dispensedBy)
                .note(note)
                .build();
        return dispensationRepo.save(dispensation);
    }

    public List<Dispensation> allDispensations() {
        return dispensationRepo.findAllByOrderByDispensedAtDesc();
    }

    public List<Dispensation> dispensationsByPatient(Long patientId) {
        return dispensationRepo.findByPatientIdOrderByDispensedAtDesc(patientId);
    }
}
