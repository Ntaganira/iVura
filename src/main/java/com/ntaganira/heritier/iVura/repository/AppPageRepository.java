package com.ntaganira.heritier.iVura.repository;

import com.ntaganira.heritier.iVura.entity.AppPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppPageRepository extends JpaRepository<AppPage, Long> {
    Optional<AppPage> findByCode(String code);
    List<AppPage> findAllByOrderBySortOrderAsc();
    List<AppPage> findByEnabledTrueOrderBySortOrderAsc();
}
