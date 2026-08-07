package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.entity.Attendance;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : AttendanceController.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Doctor attendance and shift controller
 * </pre>
 */
@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final DoctorRepository doctorRepo;
    private final ActivityLogService activityLogService;

    public AttendanceController(AttendanceService attendanceService,
                                DoctorRepository doctorRepo,
                                ActivityLogService activityLogService) {
        this.attendanceService = attendanceService;
        this.doctorRepo = doctorRepo;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_ATTENDANCE')")
    public String index(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        Model model) {
        LocalDate day = date != null ? date : LocalDate.now();
        model.addAttribute("date", day);
        model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
        model.addAttribute("attendanceMap", attendanceService.attendanceByDoctor(day));
        model.addAttribute("shiftMap", doctorRepo.findByIsActiveTrue().stream()
                .collect(java.util.stream.Collectors.toMap(
                        d -> d.getId(),
                        d -> attendanceService.shiftFor(d, day),
                        (a, b) -> a)));
        model.addAttribute("presentCount", attendanceService.presentCount(day));
        return "attendance/index";
    }

    @GetMapping("/shifts")
    @PreAuthorize("hasAuthority('PERM_MANAGE_SHIFT')")
    public String shifts(Model model) {
        model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
        model.addAttribute("days", new Integer[]{1, 2, 3, 4, 5, 6, 7});
        model.addAttribute("shiftMapsByDoctor", doctorRepo.findByIsActiveTrue().stream()
                .collect(java.util.stream.Collectors.toMap(
                        d -> d.getId(),
                        d -> attendanceService.shiftsByDay(d.getId()),
                        (a, b) -> a)));
        return "attendance/shifts";
    }

    @PostMapping("/checkin")
    @PreAuthorize("hasAuthority('PERM_CREATE_ATTENDANCE')")
    public String checkIn(@RequestParam Long doctorId,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                          RedirectAttributes ra) {
        Attendance a = attendanceService.checkIn(doctorId, date);
        activityLogService.record("Attendance", "CREATE_ATTENDANCE",
                a.getDoctor().getFullName() + " checked in on " + date, ActivityStatus.SUCCESS);
        ra.addFlashAttribute("flashSuccess", a.getDoctor().getFullName() + " checked in");
        return redirectTo(date);
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasAuthority('PERM_CREATE_ATTENDANCE')")
    public String checkOut(@RequestParam Long doctorId,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                           RedirectAttributes ra) {
        Attendance a = attendanceService.checkOut(doctorId, date);
        activityLogService.record("Attendance", "CREATE_ATTENDANCE",
                a.getDoctor().getFullName() + " checked out on " + date, ActivityStatus.SUCCESS);
        ra.addFlashAttribute("flashSuccess", a.getDoctor().getFullName() + " checked out");
        return redirectTo(date);
    }

    @PostMapping("/status")
    @PreAuthorize("hasAuthority('PERM_EDIT_ATTENDANCE')")
    public String markStatus(@RequestParam Long doctorId,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             @RequestParam String status,
                             @RequestParam(required = false) String notes,
                             RedirectAttributes ra) {
        Attendance a = attendanceService.markStatus(doctorId, date, status, notes);
        activityLogService.record("Attendance", "EDIT_ATTENDANCE",
                a.getDoctor().getFullName() + " marked " + status + " on " + date, ActivityStatus.SUCCESS);
        ra.addFlashAttribute("flashSuccess", a.getDoctor().getFullName() + " marked " + status);
        return redirectTo(date);
    }

    @PostMapping("/shifts/save")
    @PreAuthorize("hasAuthority('PERM_MANAGE_SHIFT')")
    public String saveShift(@RequestParam Long doctorId,
                            @RequestParam Integer dayOfWeek,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime start,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime end,
                            @RequestParam(required = false) String label,
                            RedirectAttributes ra) {
        attendanceService.saveShift(doctorId, dayOfWeek, start, end, label);
        activityLogService.record("Attendance", "MANAGE_SHIFT",
                "Set shift for doctor #" + doctorId + " on day " + dayOfWeek, ActivityStatus.SUCCESS);
        ra.addFlashAttribute("flashSuccess", "Shift saved");
        return "redirect:/attendance/shifts";
    }

    private String redirectTo(LocalDate date) {
        return "redirect:/attendance?date=" + date;
    }
}
