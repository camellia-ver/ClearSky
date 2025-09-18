package com.portfolio.clearSky.repository;

import com.portfolio.clearSky.model.AdministrativeBoundary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AdministrativeBoundaryRepository extends JpaRepository<AdministrativeBoundary, Long> {
}
