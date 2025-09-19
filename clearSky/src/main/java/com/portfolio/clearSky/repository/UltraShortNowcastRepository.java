package com.portfolio.clearSky.repository;

import com.portfolio.clearSky.model.UltraShortNowcast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UltraShortNowcastRepository extends JpaRepository<UltraShortNowcast, Long> {
}
