package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SpecializationRepository extends JpaRepository<Specialization, Long>, JpaSpecificationExecutor<Specialization> {
    Optional<Specialization> findByNameIgnoreCase(String name);
    List<Specialization> findAllByOrderByNameAsc();
}
