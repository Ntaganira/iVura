package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.entity.RadiologyExam;
import com.ntaganira.heritier.iVura.repository.RadiologyExamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.service
 * - File      : RadiologyExamService.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Radiology Exam Catalog Service
 * </pre>
 */
@Service
public class RadiologyExamService {

    private final RadiologyExamRepository examRepo;

    public RadiologyExamService(RadiologyExamRepository examRepo) {
        this.examRepo = examRepo;
    }

    public List<RadiologyExam> findAll() {
        return examRepo.findAll();
    }

    public List<RadiologyExam> findActive() {
        return examRepo.findByIsActiveTrueOrderByNameAsc();
    }

    public RadiologyExam findById(Long id) {
        return examRepo.findById(id).orElse(null);
    }

    @Transactional
    public RadiologyExam save(RadiologyExam exam) {
        return examRepo.save(exam);
    }

    @Transactional
    public void delete(Long id) {
        RadiologyExam exam = findById(id);
        if (exam != null) {
            exam.setIsActive(false);
            examRepo.save(exam);
        }
    }
}
