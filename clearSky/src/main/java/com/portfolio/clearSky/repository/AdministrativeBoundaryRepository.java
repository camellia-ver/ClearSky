package com.portfolio.clearSky.repository;

import com.portfolio.clearSky.model.AdministrativeBoundary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministrativeBoundaryRepository extends JpaRepository<AdministrativeBoundary, Long> {
    Optional<AdministrativeBoundary> findByAdmLevel1AndAdmLevel2AndAdmLevel3(String level1, String level2, String level3);
    Optional<AdministrativeBoundary> findByAdmLevel1AndAdmLevel2(String level1, String level2);
    Optional<AdministrativeBoundary> findByAdmLevel1(String level1);
    Optional<AdministrativeBoundary> findByAdmLevel2AndAdmLevel3(String level2, String level3);
    Optional<AdministrativeBoundary> findByAdmLevel3(String level3);
    Optional<AdministrativeBoundary> findByAdmLevel1AndAdmLevel3(String level1, String level3);
    Optional<AdministrativeBoundary> findByAdmLevel2(String level2);
}
