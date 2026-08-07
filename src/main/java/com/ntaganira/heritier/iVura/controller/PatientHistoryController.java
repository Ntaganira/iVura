package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.ImmunizationDto;
import com.ntaganira.heritier.iVura.dto.LabResultDto;
import com.ntaganira.heritier.iVura.dto.MedicalRecordDto;
import com.ntaganira.heritier.iVura.entity.Immunization;
import com.ntaganira.heritier.iVura.entity.LabResult;
import com.ntaganira.heritier.iVura.entity.MedicalRecord;
import com.ntaganira.heritier.iVura.entity.Patient;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.AppointmentRepository;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.ImmunizationService;
import com.ntaganira.heritier.iVura.service.LabResultService;
import com.ntaganira.heritier.iVura.service.MedicalRecordService;
import com.ntaganira.heritier.iVura.service.PatientService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : PatientHistoryController.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Patient History (EHR) Controller
 * </pre>
 */
@Controller
@RequestMapping("/patients/{id}")
public class PatientHistoryController {

    private final PatientService patientService;
    private final MedicalRecordService recordService;
    private final LabResultService labService;
    private final ImmunizationService immunizationService;
    private final DoctorRepository doctorRepo;
    private final AppointmentRepository appointmentRepo;
    private final ActivityLogService activityLogService;

    public PatientHistoryController(PatientService patientService,
                                    MedicalRecordService recordService,
                                    LabResultService labService,
                                    ImmunizationService immunizationService,
                                    DoctorRepository doctorRepo,
                                    AppointmentRepository appointmentRepo,
                                    ActivityLogService activityLogService) {
        this.patientService = patientService;
        this.recordService = recordService;
        this.labService = labService;
        this.immunizationService = immunizationService;
        this.doctorRepo = doctorRepo;
        this.appointmentRepo = appointmentRepo;
        this.activityLogService = activityLogService;
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('PERM_VIEW_RECORD')")
    public String history(@PathVariable Long id, Model model) {
        Patient patient = patientService.findById(id);
        model.addAttribute("patient", patient);
        model.addAttribute("records", recordService.findByPatientId(id));
        model.addAttribute("labs", labService.findByPatientId(id));
        model.addAttribute("immunizations", immunizationService.findByPatientId(id));
        model.addAttribute("appointments", appointmentRepo.findByPatientId(id));
        model.addAttribute("recordDto", new MedicalRecordDto());
        model.addAttribute("labDto", new LabResultDto());
        model.addAttribute("immunizationDto", new ImmunizationDto());
        model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
        model.addAttribute("statuses", LabResultService.STATUSES);
        return "patients/history";
    }

    @PostMapping("/record")
    @PreAuthorize("hasAuthority('PERM_CREATE_RECORD')")
    public String addRecord(@PathVariable Long id,
                            @ModelAttribute("recordDto") MedicalRecordDto dto,
                            RedirectAttributes ra) {
        dto.setPatientId(id);
        try {
            MedicalRecord record = recordService.create(dto);
            activityLogService.record("EHR / Medical Records", "CREATE_RECORD",
                    "Recorded diagnosis for " + record.getPatient().getFullName(), ActivityStatus.SUCCESS);
            ra.addFlashAttribute("flashSuccess", "Medical record saved");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/patients/" + id + "/history";
    }

    @PostMapping("/lab")
    @PreAuthorize("hasAuthority('PERM_CREATE_LAB')")
    public String addLab(@PathVariable Long id,
                         @ModelAttribute("labDto") LabResultDto dto,
                         RedirectAttributes ra) {
        dto.setPatientId(id);
        try {
            LabResult lab = labService.create(dto);
            activityLogService.record("Laboratory", "CREATE_LAB",
                    "Recorded " + lab.getTestName() + " for " + lab.getPatient().getFullName(),
                    ActivityStatus.SUCCESS);
            ra.addFlashAttribute("flashSuccess", "Lab result saved");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/patients/" + id + "/history";
    }

    @PostMapping("/immunization")
    @PreAuthorize("hasAuthority('PERM_CREATE_IMMUNIZATION')")
    public String addImmunization(@PathVariable Long id,
                                  @ModelAttribute("immunizationDto") ImmunizationDto dto,
                                  RedirectAttributes ra) {
        dto.setPatientId(id);
        try {
            Immunization immunization = immunizationService.create(dto);
            activityLogService.record("Immunization", "CREATE_IMMUNIZATION",
                    "Recorded " + immunization.getVaccine() + " for "
                            + immunization.getPatient().getFullName(), ActivityStatus.SUCCESS);
            ra.addFlashAttribute("flashSuccess", "Immunization saved");
        } catch (Exception e) {
            ra.addFlashAttribute("flashError", e.getMessage());
        }
        return "redirect:/patients/" + id + "/history";
    }

    @GetMapping("/immunization/{immunizationId}/delete")
    @PreAuthorize("hasAuthority('PERM_CREATE_IMMUNIZATION')")
    public String deleteImmunization(@PathVariable Long id, @PathVariable Long immunizationId) {
        immunizationService.delete(immunizationId);
        activityLogService.record("Immunization", "CREATE_IMMUNIZATION",
                "Deleted immunization #" + immunizationId, ActivityStatus.SUCCESS);
        return "redirect:/patients/" + id + "/history";
    }
}
