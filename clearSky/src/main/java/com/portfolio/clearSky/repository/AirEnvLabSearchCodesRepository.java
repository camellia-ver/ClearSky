package com.portfolio.clearSky.repository;

import com.portfolio.clearSky.model.AirEnvLabSearchCodes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirEnvLabSearchCodesRepository extends JpaRepository<AirEnvLabSearchCodes, Long> {
}
