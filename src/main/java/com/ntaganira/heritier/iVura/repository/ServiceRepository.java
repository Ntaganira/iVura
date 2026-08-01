package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long>, JpaSpecificationExecutor<Service> {
    Optional<Service> findByNameIgnoreCase(String name);
    List<Service> findAllByOrderByNameAsc();
}
