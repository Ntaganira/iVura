package com.ntaganira.heritier.iVura.controller;

import com.ntaganira.heritier.iVura.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.controller
 * - File      : FileUploadController.java
 * - Date      : 2026. 07. 30.
 * - User      : Hntaganira
 * - Desc      : File Upload Controller (MinIO)
 * </pre>
 */
@Controller
@RequestMapping("/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                                       @RequestParam(value = "prefix", defaultValue = "uploads") String prefix) {
        String objectName = fileStorageService.upload(file, prefix);
        String url = fileStorageService.getUrl(objectName);
        return ResponseEntity.ok(Map.of(
                "objectName", objectName,
                "url", url
        ));
    }

    @DeleteMapping("/{objectName}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable String objectName) {
        fileStorageService.delete(objectName);
        return ResponseEntity.noContent().build();
    }
}
