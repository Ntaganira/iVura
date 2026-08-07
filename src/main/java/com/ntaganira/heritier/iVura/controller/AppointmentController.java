package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.AppointmentDto;
import com.ntaganira.heritier.iVura.entity.Appointment;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.enums.AppointmentStatus;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import com.ntaganira.heritier.iVura.repository.ServiceRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final ServiceRepository serviceRepo;
    private final ActivityLogService activityLogService;

    public AppointmentController(AppointmentService appointmentService,
                                  PatientRepository patientRepo,
                                  DoctorRepository doctorRepo,
                                  ServiceRepository serviceRepo,
                                  ActivityLogService activityLogService) {
        this.appointmentService = appointmentService;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.serviceRepo = serviceRepo;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_APPOINTMENT')")
    public String list(Model model) {
        model.addAttribute("appointments", appointmentService.findAll());
        return "appointments/list";
    }

    @GetMapping("/calendar")
    @PreAuthorize("hasAuthority('PERM_VIEW_APPOINTMENT')")
    public String calendar() {
        return "appointments/calendar";
    }

    @GetMapping("/api/events")
    @PreAuthorize("hasAuthority('PERM_VIEW_APPOINTMENT')")
    @ResponseBody
    public List<Map<String, Object>> events(@RequestParam(required = false) String start,
                                            @RequestParam(required = false) String end) {
        LocalDate from = toLocalDate(start);
        LocalDate to = toLocalDate(end);
        List<Appointment> appointments = (from != null && to != null)
            ? appointmentService.findBetween(from, to)
            : appointmentService.findAll();
        return appointments.stream().map(a -> {
            LocalDateTime startDateTime = LocalDateTime.of(a.getAppointmentDate(), a.getAppointmentTime());
            Map<String, Object> event = new HashMap<>();
            event.put("id", a.getId());
            event.put("title", a.getPatient().getFullName() + " \u00b7 " + a.getDoctor().getFullName());
            event.put("start", startDateTime);
            event.put("end", startDateTime.plusMinutes(30));
            event.put("className", "cal-event-status-" + classNameForStatus(a.getStatus()));
            event.put("extendedProps", Map.of("status", a.getStatus().name()));
            return event;
        }).collect(Collectors.toList());
    }

    @GetMapping("/view/{id}")
    @PreAuthorize("hasAuthority('PERM_VIEW_APPOINTMENT')")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("appointment", appointmentService.findById(id));
        return "appointments/view";
    }

    @PostMapping("/reschedule")
    @PreAuthorize("hasAuthority('PERM_EDIT_APPOINTMENT')")
    @ResponseBody
    public Map<String, Object> reschedule(@RequestParam Long id,
                                          @RequestParam LocalDate date,
                                          @RequestParam LocalTime time) {
        Map<String, Object> body = new HashMap<>();
        try {
            appointmentService.reschedule(id, date, time);
            activityLogService.record("Appointment Management", "UPDATE_APPOINTMENT",
                "Rescheduled appointment #" + id + " to " + date + " " + time, ActivityStatus.SUCCESS);
            body.put("ok", true);
        } catch (Exception e) {
            body.put("ok", false);
            body.put("error", e.getMessage());
        }
        return body;
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_APPOINTMENT')")
    public String addForm(Model model,
                          @RequestParam(required = false) LocalDate date,
                          @RequestParam(required = false) LocalTime time) {
        AppointmentDto dto = new AppointmentDto();
        if (date != null) {
            dto.setAppointmentDate(date);
        }
        if (time != null) {
            dto.setAppointmentTime(time);
        }
        model.addAttribute("appointmentDto", dto);
        model.addAttribute("patients", patientRepo.findByIsActiveTrue());
        model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
        model.addAttribute("services", serviceRepo.findAllByOrderByNameAsc());
        return "appointments/add";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_APPOINTMENT')")
    public String add(@Valid @ModelAttribute("appointmentDto") AppointmentDto dto,
                      BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("patients", patientRepo.findByIsActiveTrue());
            model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
            model.addAttribute("services", serviceRepo.findAllByOrderByNameAsc());
            model.addAttribute("formErrors", result.getFieldErrors());
            return "appointments/add";
        }
        try {
            Appointment appointment = appointmentService.create(dto);
            String patientName = appointment.getPatient() != null ? appointment.getPatient().getFullName() : "#" + dto.getPatientId();
            activityLogService.record("Appointment Management", "CREATE_APPOINTMENT",
                    "Created appointment for patient " + patientName, ActivityStatus.SUCCESS);
        } catch (RuntimeException e) {
            model.addAttribute("flashError", e.getMessage());
            model.addAttribute("patients", patientRepo.findByIsActiveTrue());
            model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
            model.addAttribute("services", serviceRepo.findAllByOrderByNameAsc());
            return "appointments/add";
        }
        return "redirect:/appointments";
    }

    @GetMapping("/status/{id}/{status}")
    @PreAuthorize("hasAuthority('PERM_EDIT_APPOINTMENT')")
    public String updateStatus(@PathVariable Long id, @PathVariable String status) {
        Appointment appointment = appointmentService.updateStatus(id, status);
        activityLogService.record("Appointment Management", "UPDATE_APPOINTMENT",
                "Changed appointment #" + id + " status to " + appointment.getStatus(), ActivityStatus.SUCCESS);
        return "redirect:/appointments";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_CANCEL_APPOINTMENT')")
    public String delete(@PathVariable Long id) {
        appointmentService.delete(id);
        activityLogService.record("Appointment Management", "CANCEL_APPOINTMENT",
                "Cancelled appointment #" + id, ActivityStatus.SUCCESS);
        return "redirect:/appointments";
    }

    private LocalDate toLocalDate(String iso) {
        if (iso == null || iso.isEmpty()) {
            return null;
        }
        return LocalDate.parse(iso.length() >= 10 ? iso.substring(0, 10) : iso);
    }

    private String classNameForStatus(AppointmentStatus status) {
        switch (status) {
            case CONFIRMED:
                return "confirmed";
            case IN_PROGRESS:
                return "in-progress";
            case COMPLETED:
                return "completed";
            case CANCELLED:
                return "cancelled";
            case NO_SHOW:
                return "no-show";
            default:
                return "scheduled";
        }
    }
}
