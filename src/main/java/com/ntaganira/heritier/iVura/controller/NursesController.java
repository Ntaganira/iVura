package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : NursesController.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Nursing staff directory controller
 * </pre>
 */
@Controller
@RequestMapping("/nurses")
public class NursesController {

    private final UserRepository userRepo;

    public NursesController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PAGE_NURSES')")
    public String index(Model model) {
        model.addAttribute("nurses", userRepo.findByRoles_Code("NURSE"));
        return "nurses/index";
    }
}
