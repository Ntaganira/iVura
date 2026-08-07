package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.dto.MedicalRecordDto;
import com.ntaganira.heritier.iVura.entity.MedicalRecord;
import com.ntaganira.heritier.iVura.enums.ActivityStatus;
import com.ntaganira.heritier.iVura.repository.AppointmentRepository;
import com.ntaganira.heritier.iVura.repository.DoctorRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import com.ntaganira.heritier.iVura.service.ActivityLogService;
import com.ntaganira.heritier.iVura.service.MedicalRecordService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : MedicalRecordController.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Medical Record Controller
 * </pre>
 */
@Controller
@RequestMapping("/medical-records")
public class MedicalRecordController {

    private static final int PAGE_SIZE = 10;

    private final MedicalRecordService recordService;
    private final PatientRepository patientRepo;
    private final DoctorRepository doctorRepo;
    private final AppointmentRepository appointmentRepo;
    private final ActivityLogService activityLogService;

    public MedicalRecordController(MedicalRecordService recordService,
                                   PatientRepository patientRepo,
                                   DoctorRepository doctorRepo,
                                   AppointmentRepository appointmentRepo,
                                   ActivityLogService activityLogService) {
        this.recordService = recordService;
        this.patientRepo = patientRepo;
        this.doctorRepo = doctorRepo;
        this.appointmentRepo = appointmentRepo;
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_RECORD')")
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<MedicalRecord> records = recordService.findPage(page, PAGE_SIZE);
        model.addAttribute("records", records);
        model.addAttribute("total", records.getTotalElements());
        model.addAttribute("paginationQuery", "");
        return "medical-records/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_RECORD')")
    public String addForm(@RequestParam(required = false) Long patientId, Model model) {
        MedicalRecordDto dto = new MedicalRecordDto();
        if (patientId != null) {
            dto.setPatientId(patientId);
        }
        model.addAttribute("recordDto", dto);
        addFormOptions(model);
        return "medical-records/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_CREATE_RECORD')")
    public String add(@Valid @ModelAttribute("recordDto") MedicalRecordDto dto,
                      BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formErrors", result.getFieldErrors());
            addFormOptions(model);
            return "medical-records/form";
        }
        MedicalRecord record = recordService.create(dto);
        activityLogService.record("EHR / Medical Records", "CREATE_RECORD",
                "Recorded diagnosis for patient " + record.getPatient().getFullName(),
                ActivityStatus.SUCCESS);
        return "redirect:/medical-records";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_RECORD')")
    public String editForm(@PathVariable Long id, Model model) {
        MedicalRecord record = recordService.findById(id);
        MedicalRecordDto dto = new MedicalRecordDto();
        dto.setId(record.getId());
        dto.setPatientId(record.getPatient().getId());
        dto.setDoctorId(record.getDoctor() != null ? record.getDoctor().getId() : null);
        dto.setAppointmentId(record.getAppointment() != null ? record.getAppointment().getId() : null);
        dto.setDiagnosis(record.getDiagnosis());
        dto.setPrescription(record.getPrescription());
        dto.setNotes(record.getNotes());
        dto.setRecordDate(record.getRecordDate());
        model.addAttribute("recordDto", dto);
        addFormOptions(model);
        return "medical-records/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_EDIT_RECORD')")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("recordDto") MedicalRecordDto dto,
                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formErrors", result.getFieldErrors());
            addFormOptions(model);
            return "medical-records/form";
        }
        MedicalRecord record = recordService.update(id, dto);
        activityLogService.record("EHR / Medical Records", "EDIT_RECORD",
                "Updated medical record #" + id + " for " + record.getPatient().getFullName(),
                ActivityStatus.SUCCESS);
        return "redirect:/medical-records";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_DELETE_RECORD')")
    public String delete(@PathVariable Long id) {
        MedicalRecord record = recordService.findById(id);
        recordService.delete(id);
        activityLogService.record("EHR / Medical Records", "DELETE_RECORD",
                "Deleted medical record #" + id, ActivityStatus.SUCCESS);
        return "redirect:/medical-records";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("patients", patientRepo.findByIsActiveTrue());
        model.addAttribute("doctors", doctorRepo.findByIsActiveTrue());
        model.addAttribute("appointments", appointmentRepo.findAll());
    }
}
