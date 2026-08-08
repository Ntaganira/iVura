package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.InstrumentMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : InstrumentMessageRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Instrument Message Repository
 * </pre>
 */
public interface InstrumentMessageRepository extends JpaRepository<InstrumentMessage, Long> {
    Optional<InstrumentMessage> findByMessageId(String messageId);
    List<InstrumentMessage> findByStatusOrderByCreatedAtDesc(String status);
    List<InstrumentMessage> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
}
