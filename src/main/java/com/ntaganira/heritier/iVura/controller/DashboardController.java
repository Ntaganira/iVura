package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.entity.Appointment;
import com.ntaganira.heritier.iVura.enums.AppointmentStatus;
import com.ntaganira.heritier.iVura.repository.AppointmentRepository;
import com.ntaganira.heritier.iVura.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final AppointmentRepository appointmentRepo;

    public DashboardController(DashboardService dashboardService, AppointmentRepository appointmentRepo) {
        this.dashboardService = dashboardService;
        this.appointmentRepo = appointmentRepo;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalPatients", dashboardService.getTotalPatients());
        model.addAttribute("totalDoctors", dashboardService.getTotalDoctors());
        model.addAttribute("todayAppointments", dashboardService.getTodayAppointments());
        model.addAttribute("totalDepartments", dashboardService.getTotalDepartments());
        model.addAttribute("completedToday", dashboardService.getCompletedAppointmentsToday());
        model.addAttribute("pendingAppointments", dashboardService.getPendingAppointments());
        model.addAttribute("weeklyData", dashboardService.getWeeklyAppointments());

        List<Appointment> todayList = appointmentRepo.findByAppointmentDate(LocalDate.now());
        model.addAttribute("todayAppointmentList", todayList);

        return "dashboard";
    }
}
