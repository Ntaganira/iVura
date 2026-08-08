package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.entity.RadiologyExam;
import com.ntaganira.heritier.iVura.service.RadiologyExamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : RadiologyCatalogController.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Radiology Exam Catalog Controller
 * </pre>
 */
@Controller
@RequestMapping("/radiology-catalog")
public class RadiologyCatalogController {

    private final RadiologyExamService examService;

    public RadiologyCatalogController(RadiologyExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_RAD_CATALOG')")
    public String list(Model model) {
        model.addAttribute("exams", examService.findAll());
        return "radiology-catalog/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PERM_MANAGE_RAD_CATALOG')")
    public String addForm(Model model) {
        model.addAttribute("exam", new RadiologyExam());
        return "radiology-catalog/form";
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('PERM_MANAGE_RAD_CATALOG')")
    public String add(@ModelAttribute RadiologyExam exam, RedirectAttributes ra) {
        examService.save(exam);
        ra.addFlashAttribute("flashSuccess", "Exam " + exam.getCode() + " added");
        return "redirect:/radiology-catalog";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_MANAGE_RAD_CATALOG')")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("exam", examService.findById(id));
        return "radiology-catalog/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PERM_MANAGE_RAD_CATALOG')")
    public String edit(@PathVariable Long id, @ModelAttribute RadiologyExam exam, RedirectAttributes ra) {
        RadiologyExam existing = examService.findById(id);
        if (existing == null) {
            ra.addFlashAttribute("flashError", "Exam not found");
            return "redirect:/radiology-catalog";
        }
        existing.setCode(exam.getCode());
        existing.setName(exam.getName());
        existing.setModality(exam.getModality());
        existing.setBodyPart(exam.getBodyPart());
        existing.setPrice(exam.getPrice());
        existing.setIsActive(exam.getIsActive() != null ? exam.getIsActive() : true);
        examService.save(existing);
        ra.addFlashAttribute("flashSuccess", "Exam " + existing.getCode() + " updated");
        return "redirect:/radiology-catalog";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PERM_MANAGE_RAD_CATALOG')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        examService.delete(id);
        ra.addFlashAttribute("flashSuccess", "Exam deactivated");
        return "redirect:/radiology-catalog";
    }
}
