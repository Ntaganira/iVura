package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.RoomStay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 * - Project   : iVura - Hospital Management System
 * - Package   : com.ntaganira.heritier.iVura.repository
 * - File      : RoomStayRepository.java
 * - Date      : 2026. 08. 08.
 * - User      : Hntaganira
 * - Desc      : Room Stay Repository
 * </pre>
 */
public interface RoomStayRepository extends JpaRepository<RoomStay, Long> {
    List<RoomStay> findByStatusOrderByCheckInDateDesc(String status);
    List<RoomStay> findAllByOrderByCheckInDateDesc();
    List<RoomStay> findByPatientIdOrderByCheckInDateDesc(Long patientId);
    List<RoomStay> findByRoomIdAndStatus(Long roomId, String status);
    long countByStatus(String status);
    Optional<RoomStay> findFirstByPatientIdAndStatusOrderByIdDesc(Long patientId, String status);
}
