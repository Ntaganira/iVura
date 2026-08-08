package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.entity.RoomStay;
import com.ntaganira.heritier.iVura.entity.WardRoom;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.AdmissionsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : AdmissionsController.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Ward admissions, room stays and discharge controller
 * </pre>
 */
@Controller
@RequestMapping("/admissions")
public class AdmissionsController {

    private final AdmissionsService admissionsService;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final ActivityLogService activityLogService;

    public AdmissionsController(AdmissionsService admissionsService,
                                PatientRepository patientRepo,
                                DoctorRepository doctorRepo,
                                ActivityLogService activityLogService) {
        this.admissionsService = admissionsService;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_ROOM')")
    public String index(Model model) {
        model.addAttribute("admitted", admissionsService.admittedStays());
        model.addAttribute("rooms", admissionsService.findAllRooms());
        model.addAttribute("admittedCount", admissionsService.admittedCount());
        model.addAttribute("dischargedCount", admissionsService.dischargedCount());
        model.addAttribute("patients", patientRepo.findByIsActiveTrue());
        model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
        return "admissions/index";
    }

    @GetMapping("/stays")
    @PreAuthorize("hasAuthority('PERM_VIEW_ROOM')")
    public String stays(Model model) {
        model.addAttribute("stays", admissionsService.allStays());
        return "admissions/stays";
    }

    @GetMapping("/rooms")
    @PreAuthorize("hasAuthority('PERM_VIEW_ROOM')")
    public String rooms(Model model) {
        model.addAttribute("rooms", admissionsService.findAllRooms());
        model.addAttribute("room", new WardRoom());
        return "admissions/rooms";
    }

    @GetMapping("/rooms/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_MANAGE_ROOM')")
    public String editRoomForm(@PathVariable Long id, Model model) {
        model.addAttribute("rooms", admissionsService.findAllRooms());
        model.addAttribute("room", admissionsService.findRoom(id));
        return "admissions/rooms";
    }

    @PostMapping("/rooms/add")
    @PreAuthorize("hasAuthority('PERM_MANAGE_ROOM')")
    public String addRoom(@ModelAttribute WardRoom room, RedirectAttributes ra) {
        WardRoom saved = admissionsService.saveRoom(room, null);
        activityLogService.record("Admissions", "MANAGE_ROOM",
                "Added room " + saved.getRoomNumber(), ActivityStatus.SUCCESS);
        ra.addFlashAttribute("flashSuccess", "Room " + saved.getRoomNumber() + " added");
        return "redirect:/admissions/rooms";
    }

    @PostMapping("/rooms/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_MANAGE_ROOM')")
    public String editRoom(@PathVariable Long id, @ModelAttribute WardRoom room, RedirectAttributes ra) {
        WardRoom saved = admissionsService.saveRoom(room, id);
        activityLogService.record("Admissions", "MANAGE_ROOM",
                "Updated room " + saved.getRoomNumber(), ActivityStatus.SUCCESS);
        ra.addFlashAttribute("flashSuccess", "Room " + saved.getRoomNumber() + " updated");
        return "redirect:/admissions/rooms";
    }

    @PostMapping("/admit")
    @PreAuthorize("hasAuthority('PERM_ADMIT_PATIENT')")
    public String admit(@RequestParam Long patientId,
                        @RequestParam Long roomId,
                        @RequestParam(required = false) Long doctorId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
                        @RequestParam(required = false) String notes,
                        RedirectAttributes ra) {
        try {
            RoomStay stay = admissionsService.admit(patientId, roomId, doctorId, checkInDate, notes);
            activityLogService.record("Admissions", "ADMIT_PATIENT",
                    "Admitted " + stay.getPatient().getFullName() + " to room "
                            + stay.getRoom().getRoomNumber(),
                    ActivityStatus.SUCCESS);
            ra.addFlashAttribute("flashSuccess", stay.getPatient().getFullName() + " admitted to room "
                    + stay.getRoom().getRoomNumber());
        } catch (RuntimeException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/admissions";
    }

    @PostMapping("/discharge/{id}")
    @PreAuthorize("hasAuthority('PERM_DISCHARGE_PATIENT')")
    public String discharge(@PathVariable Long id,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
                            RedirectAttributes ra) {
        try {
            RoomStay stay = admissionsService.discharge(id, checkOutDate);
            activityLogService.record("Admissions", "DISCHARGE_PATIENT",
                    "Discharged " + stay.getPatient().getFullName() + " from room "
                            + stay.getRoom().getRoomNumber() + " (bill "
                            + (stay.getBilling() != null ? stay.getBilling().getInvoiceNumber() : "n/a") + ")",
                    ActivityStatus.SUCCESS);
            ra.addFlashAttribute("flashSuccess", stay.getPatient().getFullName() + " discharged. Bill "
                    + (stay.getBilling() != null ? stay.getBilling().getInvoiceNumber() : "generated"));
        } catch (RuntimeException e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/admissions";
    }
}
