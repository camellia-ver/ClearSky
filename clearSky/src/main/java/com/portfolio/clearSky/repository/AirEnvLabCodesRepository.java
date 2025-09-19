package com.portfolio.clearSky.repository;

import com.portfolio.clearSky.model.AirEnvLabCodes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirEnvLabCodesRepository extends JpaRepository<AirEnvLabCodes, Long> {
}
