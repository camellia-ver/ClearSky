package com.portfolio.clearSky.repository;

import com.portfolio.clearSky.model.ForecastCodes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForecastCodesRepository extends JpaRepository<ForecastCodes, Long> {
}
