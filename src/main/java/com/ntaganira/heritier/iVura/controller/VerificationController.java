package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.entity.LabResult;
import com.ntaganira.heritier.iVura.entity.User;
import com.ntaganira.heritier.iVura.repository.UserRepository;
import com.ntaganira.heritier.iVura.service.VerificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : VerificationController.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Lab Result Verification Workbench Controller
 * </pre>
 */
@Controller
@RequestMapping("/lab-verify")
public class VerificationController {

    private final VerificationService verificationService;
    private final UserRepository userRepo;

    public VerificationController(VerificationService verificationService,
                                  UserRepository userRepo) {
        this.verificationService = verificationService;
        this.userRepo = userRepo;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VERIFY_LAB')")
    public String worklist(Model model) {
        model.addAttribute("results", verificationService.worklist());
        return "lab-verify/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_VERIFY_LAB')")
    public String detail(@PathVariable Long id, Model model) {
        LabResult result = verificationService.findById(id);
        if (result == null) {
            return "redirect:/lab-verify";
        }
        model.addAttribute("result", result);
        model.addAttribute("signoffs", verificationService.signoffsOf(result));
        return "lab-verify/detail";
    }

    @PostMapping("/verify/{id}")
    @PreAuthorize("hasAuthority('PERM_VERIFY_LAB')")
    public String verify(@PathVariable Long id, RedirectAttributes ra) {
        verificationService.verify(id, currentUser());
        ra.addFlashAttribute("flashSuccess", "Result verified");
        return "redirect:/lab-verify";
    }

    @PostMapping("/publish/{id}")
    @PreAuthorize("hasAuthority('PERM_VERIFY_LAB')")
    public String publish(@PathVariable Long id, RedirectAttributes ra) {
        LabResult result = verificationService.findById(id);
        if (result != null && result.getFlag() != null && result.getFlag().startsWith("CRITICAL")) {
            if (!currentUserCanApproveCritical()) {
                ra.addFlashAttribute("flashError",
                        "This result has a CRITICAL flag and requires APPROVE_CRITICAL_LAB authority");
                return "redirect:/lab-verify/" + id;
            }
        }
        verificationService.publish(id, currentUser());
        ra.addFlashAttribute("flashSuccess", "Result published to patient record");
        return "redirect:/lab-verify";
    }

    @PostMapping("/hold/{id}")
    @PreAuthorize("hasAuthority('PERM_VERIFY_LAB')")
    public String hold(@PathVariable Long id, @RequestParam(required = false) String reason,
                       RedirectAttributes ra) {
        verificationService.hold(id, currentUser(), reason);
        ra.addFlashAttribute("flashSuccess", "Result held");
        return "redirect:/lab-verify";
    }

    @PostMapping("/reject/{id}")
    @PreAuthorize("hasAuthority('PERM_VERIFY_LAB')")
    public String reject(@PathVariable Long id, @RequestParam(required = false) String reason,
                         RedirectAttributes ra) {
        verificationService.reject(id, currentUser(), reason);
        ra.addFlashAttribute("flashSuccess", "Result rejected");
        return "redirect:/lab-verify";
    }

    @PostMapping("/override/{id}")
    @PreAuthorize("hasAuthority('PERM_VERIFY_LAB')")
    public String override(@PathVariable Long id,
                           @RequestParam String value,
                           @RequestParam String reason,
                           RedirectAttributes ra) {
        verificationService.override(id, value, reason, currentUser());
        ra.addFlashAttribute("flashSuccess", "Result overridden - reason logged");
        return "redirect:/lab-verify/" + id;
    }

    private boolean currentUserCanApproveCritical() {
        User user = currentUser();
        if (user == null) {
            return false;
        }
        return user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .anyMatch(p -> "APPROVE_CRITICAL_LAB".equals(p.getCode()));
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? userRepo.findByUsername(auth.getName()).orElse(null) : null;
    }
}
