package com.portfolio.clearSky.repository;

import com.portfolio.clearSky.model.AdministrativeBoundary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministrativeBoundaryRepository extends JpaRepository<AdministrativeBoundary, Long> {
    Optional<AdministrativeBoundary> findByAdmLevel1AndAdmLevel2IsNullAndAdmLevel3IsNull(String level1);
    Optional<AdministrativeBoundary> findByAdmLevel1AndAdmLevel2ContainingAndAdmLevel3IsNull(String level1, String level2);
    Optional<AdministrativeBoundary> findByAdmLevel1AndAdmLevel2ContainingAndAdmLevel3Containing(String level1, String level2, String level3);
}
