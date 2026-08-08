package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.entity.AnalyzerDevice;
import com.ntaganira.heritier.iVura.repository.AnalyzerDeviceRepository;
import com.ntaganira.heritier.iVura.repository.InstrumentMessageRepository;
import com.ntaganira.heritier.iVura.service.InstrumentGateway;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : IntegrationController.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Lab Integration (error console + analyzer devices) Controller
 * </pre>
 */
@Controller
@RequestMapping("/lab-integration")
public class IntegrationController {

    private final InstrumentGateway gateway;
    private final InstrumentMessageRepository messageRepo;
    private final AnalyzerDeviceRepository deviceRepo;

    public IntegrationController(InstrumentGateway gateway,
                                 InstrumentMessageRepository messageRepo,
                                 AnalyzerDeviceRepository deviceRepo) {
        this.gateway = gateway;
        this.messageRepo = messageRepo;
        this.deviceRepo = deviceRepo;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_VIEW_INTEGRATION_LOGS')")
    public String index(@RequestParam(required = false) String status, Model model) {
        model.addAttribute("messages", status != null && !status.isBlank()
                ? messageRepo.findByStatusOrderByCreatedAtDesc(status)
                : messageRepo.findAllByOrderByCreatedAtDesc());
        model.addAttribute("status", status);
        model.addAttribute("devices", deviceRepo.findAllByOrderByNameAsc());
        model.addAttribute("counts",
                java.util.Map.of("unmatched", messageRepo.countByStatus("UNMATCHED"),
                        "incomplete", messageRepo.countByStatus("INCOMPLETE"),
                        "errors", messageRepo.countByStatus("ERROR")));
        return "lab-integration/index";
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('PERM_VIEW_INTEGRATION_LOGS')")
    public String importInbox(RedirectAttributes ra) {
        int imported = gateway.importInbox();
        ra.addFlashAttribute("flashSuccess", "Inbox scan complete - " + imported + " result row(s) imported");
        return "redirect:/lab-integration";
    }

    @GetMapping("/message/{id}")
    @PreAuthorize("hasAuthority('PERM_VIEW_INTEGRATION_LOGS')")
    public String messageDetail(@PathVariable Long id, Model model) {
        model.addAttribute("message", messageRepo.findById(id).orElse(null));
        return "lab-integration/message";
    }

    @PostMapping("/retry/{id}")
    @PreAuthorize("hasAuthority('PERM_VIEW_INTEGRATION_LOGS')")
    public String retry(@PathVariable Long id, RedirectAttributes ra) {
        gateway.reprocess(id);
        ra.addFlashAttribute("flashSuccess", "Message re-processed");
        return "redirect:/lab-integration";
    }

    @GetMapping("/devices/add")
    @PreAuthorize("hasAuthority('PERM_CONFIGURE_DEVICES')")
    public String addDeviceForm(Model model) {
        model.addAttribute("device", new AnalyzerDevice());
        return "lab-integration/device-form";
    }

    @PostMapping("/devices/add")
    @PreAuthorize("hasAuthority('PERM_CONFIGURE_DEVICES')")
    public String addDevice(@ModelAttribute AnalyzerDevice device, RedirectAttributes ra) {
        deviceRepo.save(device);
        ra.addFlashAttribute("flashSuccess", "Analyzer " + device.getCode() + " registered");
        return "redirect:/lab-integration";
    }
}
