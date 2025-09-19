package com.portfolio.clearSky.repository;

import com.portfolio.clearSky.model.UltraShortForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UltraShortForecastRepository extends JpaRepository<UltraShortForecast, Long> {
}
