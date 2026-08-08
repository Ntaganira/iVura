package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.BillingDto;
import com.ntaganira.heritier.iVura.entity.Billing;
import com.ntaganira.heritier.iVura.entity.Doctor;
import com.ntaganira.heritier.iVura.entity.Patient;
import com.ntaganira.heritier.iVura.entity.RoomStay;
import com.ntaganira.heritier.iVura.entity.WardRoom;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import com.ntaganira.heritier.iVura.repository.RoomStayRepository;
import com.ntaganira.heritier.iVura.repository.WardRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.service
 * - File      : AdmissionsService.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Ward admissions, room stays and discharge billing
 * </pre>
 */
@Service
public class AdmissionsService {

    public static final String STATUS_ADMITTED = "ADMITTED";
    public static final String STATUS_DISCHARGED = "DISCHARGED";

    private final WardRoomRepository roomRepo;
    private final RoomStayRepository stayRepo;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final BillingService billingService;

    public AdmissionsService(WardRoomRepository roomRepo,
                             RoomStayRepository stayRepo,
                             PatientRepository patientRepo,
                             DoctorRepository doctorRepo,
                             BillingService billingService) {
        this.roomRepo = roomRepo;
        this.stayRepo = stayRepo;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.billingService = billingService;
    }

    public List<WardRoom> findAllRooms() {
        return roomRepo.findByIsActiveTrueOrderByRoomNumberAsc();
    }

    public WardRoom findRoom(Long id) {
        return roomRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
    }

    @Transactional
    public WardRoom saveRoom(WardRoom room, Long id) {
        if (id != null) {
            WardRoom existing = findRoom(id);
            room.setId(id);
            room.setCreatedAt(existing.getCreatedAt());
            room.setIsActive(existing.getIsActive() != null ? existing.getIsActive() : true);
        }
        if (room.getIsActive() == null) {
            room.setIsActive(true);
        }
        if (room.getCapacity() == null) {
            room.setCapacity(1);
        }
        if (room.getPricePerNight() == null) {
            room.setPricePerNight(BigDecimal.ZERO);
        }
        return roomRepo.save(room);
    }

    public List<RoomStay> allStays() {
        return stayRepo.findAllByOrderByCheckInDateDesc();
    }

    public List<RoomStay> admittedStays() {
        return stayRepo.findByStatusOrderByCheckInDateDesc(STATUS_ADMITTED);
    }

    public List<RoomStay> staysByPatient(Long patientId) {
        return stayRepo.findByPatientIdOrderByCheckInDateDesc(patientId);
    }

    public long admittedCount() {
        return stayRepo.countByStatus(STATUS_ADMITTED);
    }

    public long dischargedCount() {
        return stayRepo.countByStatus(STATUS_DISCHARGED);
    }

    @Transactional
    public RoomStay admit(Long patientId, Long roomId, Long doctorId, LocalDate checkInDate, String notes) {
        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        WardRoom room = findRoom(roomId);
        Doctor doctor = doctorId != null ? doctorRepo.findById(doctorId).orElse(null) : null;

        boolean alreadyAdmitted = stayRepo.findFirstByPatientIdAndStatusOrderByIdDesc(patientId, STATUS_ADMITTED)
                .isPresent();
        if (alreadyAdmitted) {
            throw new RuntimeException("Patient is already admitted");
        }
        boolean roomOccupied = !stayRepo.findByRoomIdAndStatus(roomId, STATUS_ADMITTED).isEmpty();
        if (roomOccupied) {
            throw new RuntimeException("Room " + room.getRoomNumber() + " is already occupied");
        }

        LocalDate day = checkInDate != null ? checkInDate : LocalDate.now();
        RoomStay stay = RoomStay.builder()
                .patient(patient)
                .room(room)
                .doctor(doctor)
                .checkInDate(day)
                .dailyRate(room.getPricePerNight() != null ? room.getPricePerNight() : BigDecimal.ZERO)
                .status(STATUS_ADMITTED)
                .notes(notes)
                .build();
        return stayRepo.save(stay);
    }

    @Transactional
    public RoomStay discharge(Long stayId, LocalDate checkOutDate) {
        RoomStay stay = stayRepo.findById(stayId)
                .orElseThrow(() -> new RuntimeException("Room stay not found with id: " + stayId));
        if (!STATUS_ADMITTED.equals(stay.getStatus())) {
            throw new RuntimeException("This stay is already discharged");
        }
        LocalDate out = checkOutDate != null ? checkOutDate : LocalDate.now();
        long nights = Math.max(1, ChronoUnit.DAYS.between(stay.getCheckInDate(), out) + 1);
        BigDecimal rate = stay.getDailyRate() != null ? stay.getDailyRate() : BigDecimal.ZERO;
        BigDecimal total = rate.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);

        BillingDto dto = new BillingDto();
        dto.setPatientId(stay.getPatient().getId());
        dto.setAmount(total);
        dto.setTax(BigDecimal.ZERO);
        dto.setDiscount(BigDecimal.ZERO);
        dto.setNotes("Room stay " + stay.getRoom().getRoomNumber() + " for "
                + nights + " night(s) from " + stay.getCheckInDate() + " to " + out);
        Billing billing = billingService.create(dto);

        stay.setCheckOutDate(out);
        stay.setStatus(STATUS_DISCHARGED);
        stay.setBilling(billing);
        return stayRepo.save(stay);
    }
}
