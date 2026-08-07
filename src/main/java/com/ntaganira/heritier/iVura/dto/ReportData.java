package com.ntaganira.heritier.iVura.dto;

import com.ntaganira.heritier.iVura.entity.Billing;
import com.ntaganira.heritier.iVura.entity.Payment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.dto
 * - File      : ReportData.java
 * - Desc      : Aggregated report data for the reports dashboard and PDF export
 * </pre>
 */
@Data
public class ReportData {

    private LocalDate from;
    private LocalDate to;

    private BigDecimal totalBilled;
    private BigDecimal totalCollected;
    private BigDecimal outstanding;
    private long billsCount;
    private long pendingBills;
    private long patientsCount;
    private long appointmentsCount;

    private List<Object[]> revenueByDay;
    private List<Object[]> collectionsByMethod;
    private List<Object[]> billsByStatus;
    private List<Object[]> topServices;
    private List<Object[]> appointmentsByDoctor;

    private List<Payment> recentPayments;
    private List<Billing> recentBills;
}
