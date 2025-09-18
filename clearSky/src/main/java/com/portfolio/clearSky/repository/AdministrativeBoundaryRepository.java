package com.portfolio.clearSky.repository;

import com.portfolio.clearSky.model.AdministrativeBoundary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AdministrativeBoundaryRepository extends JpaRepository<AdministrativeBoundary, Long> {
    List<AdministrativeBoundary> findByName(String name);
    @Query("SELECT a FROM AdministrativeBoundary a " +
            "WHERE a.longitude.sec100 BETWEEN :lonMin AND :lonMax " +
            "AND a.latitude.sec100 BETWEEN :latMin AND :latMax")
    List<AdministrativeBoundary> findWithinRange(
            @Param("lonMin") BigDecimal lonMin,
            @Param("lonMax") BigDecimal lonMax,
            @Param("latMin") BigDecimal latMin,
            @Param("latMax") BigDecimal latMax
    );
}
