package com.ntaganira.heritier.iVura.service;

import com.ntaganira.heritier.iVura.dto.ReportData;
import com.ntaganira.heritier.iVura.repository.AppointmentRepository;
import com.ntaganira.heritier.iVura.repository.BillingRepository;
import com.ntaganira.heritier.iVura.repository.PatientRepository;
import com.ntaganira.heritier.iVura.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final BillingRepository billingRepo;
    private final PaymentRepository paymentRepo;
    private final AppointmentRepository appointmentRepo;
    private final PatientRepository patientRepo;

    public ReportService(BillingRepository billingRepo, PaymentRepository paymentRepo,
                         AppointmentRepository appointmentRepo, PatientRepository patientRepo) {
        this.billingRepo = billingRepo;
        this.paymentRepo = paymentRepo;
        this.appointmentRepo = appointmentRepo;
        this.patientRepo = patientRepo;
    }

    public ReportData buildReport(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        ReportData report = new ReportData();
        report.setFrom(from);
        report.setTo(to);

        BigDecimal totalBilled = billingRepo.sumTotalBetween(start, end);
        BigDecimal totalCollected = paymentRepo.sumBetween(start, end);
        report.setTotalBilled(totalBilled);
        report.setTotalCollected(totalCollected);
        report.setOutstanding(totalBilled.subtract(totalCollected).max(BigDecimal.ZERO));
        report.setBillsCount(billingRepo.countBetween(start, end));
        report.setPendingBills(billingRepo.countPendingBetween(start, end));
        report.setPatientsCount(patientRepo.countCreatedBetween(start, end));
        report.setAppointmentsCount(appointmentRepo.countBetween(from, to));

        report.setRevenueByDay(fillMissingDays(from, to,
                paymentRepo.sumByDayBetween(start, end)));
        report.setCollectionsByMethod(paymentRepo.sumByMethodBetween(start, end));
        report.setBillsByStatus(billingRepo.statusStatsBetween(start, end));
        report.setTopServices(billingRepo.sumByServiceBetween(start, end));
        report.setAppointmentsByDoctor(appointmentRepo.countByDoctorBetween(from, to));
        report.setRecentPayments(paymentRepo.findTop10ByPaymentDateBetweenOrderByPaymentDateDesc(start, end));
        report.setRecentBills(billingRepo.findTop10ByCreatedAtBetweenOrderByCreatedAtDesc(start, end));
        return report;
    }

    private List<Object[]> fillMissingDays(LocalDate from, LocalDate to,
                                           List<Object[]> rows) {
        Map<LocalDate, BigDecimal> byDay = rows.stream()
                .collect(Collectors.toMap(r -> LocalDate.parse(String.valueOf(r[0])),
                        r -> (BigDecimal) r[1]));
        List<Object[]> filled = new ArrayList<>();
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            filled.add(new Object[]{cursor.toString(), byDay.getOrDefault(cursor, BigDecimal.ZERO)});
            cursor = cursor.plusDays(1);
        }
        return filled;
    }
}
