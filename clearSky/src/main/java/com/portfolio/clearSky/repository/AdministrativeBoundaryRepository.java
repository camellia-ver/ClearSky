package com.portfolio.clearSky.repository;

import com.portfolio.clearSky.model.AdministrativeBoundary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdministrativeBoundaryRepository extends JpaRepository<AdministrativeBoundary, Long> {
    // 정확히 일치하는 행정구역 검색
    Optional<AdministrativeBoundary> findByAdmLevel1AndAdmLevel2AndAdmLevel3(String level1, String level2, String level3);
    // 일부만 입력했을 때 검색 (like)
    Optional<AdministrativeBoundary> findByAdmLevel1AndAdmLevel2(String level1, String level2);
    Optional<AdministrativeBoundary> findByAdmLevel1(String level1);
}
