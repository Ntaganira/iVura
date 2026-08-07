package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p WHERE p.billing.id = :billingId")
    BigDecimal sumByBillingId(@Param("billingId") Long billingId);

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p WHERE p.paymentDate BETWEEN :from AND :to")
    BigDecimal sumBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p GROUP BY p.paymentMethod")
    List<Object[]> countByMethod();

    Optional<Payment> findTopByBillingIdOrderByPaymentDateDesc(Long billingId);
}
