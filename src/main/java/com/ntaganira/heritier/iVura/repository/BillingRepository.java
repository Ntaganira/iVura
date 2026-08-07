package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BillingRepository extends JpaRepository<Billing, Long>, JpaSpecificationExecutor<Billing> {
    List<Billing> findByPatientId(Long patientId);
    List<Billing> findByStatus(String status);
    List<Billing> findByStatusIn(List<String> statuses);
    long countByStatus(String status);
    Billing findTopByOrderByIdDesc();
}
