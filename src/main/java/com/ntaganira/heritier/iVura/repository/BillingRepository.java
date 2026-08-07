package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface BillingRepository extends JpaRepository<Billing, Long>, JpaSpecificationExecutor<Billing> {
    List<Billing> findByPatientId(Long patientId);
    List<Billing> findByStatus(String status);
    List<Billing> findByStatusIn(List<String> statuses);
    long countByStatus(String status);
    Billing findTopByOrderByIdDesc();

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Billing b WHERE b.createdAt BETWEEN :from AND :to")
    BigDecimal sumTotalBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(b) FROM Billing b WHERE b.createdAt BETWEEN :from AND :to")
    long countBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(b) FROM Billing b WHERE b.status IN ('PENDING', 'PARTIALLY_PAID') " +
            "AND b.createdAt BETWEEN :from AND :to")
    long countPendingBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<Billing> findTop10ByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);

    @Query("SELECT b.status, COUNT(b), COALESCE(SUM(b.totalAmount), 0) FROM Billing b " +
            "WHERE b.createdAt BETWEEN :from AND :to GROUP BY b.status")
    List<Object[]> statusStatsBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT b.appointment.service.name, COALESCE(SUM(b.totalAmount), 0) FROM Billing b " +
            "WHERE b.appointment IS NOT NULL AND b.appointment.service IS NOT NULL " +
            "AND b.createdAt BETWEEN :from AND :to " +
            "GROUP BY b.appointment.service.name ORDER BY COALESCE(SUM(b.totalAmount), 0) DESC")
    List<Object[]> sumByServiceBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
