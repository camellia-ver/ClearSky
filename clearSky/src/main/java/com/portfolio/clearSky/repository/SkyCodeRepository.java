package com.portfolio.clearSky.repository;

import com.portfolio.clearSky.model.SkyCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkyCodeRepository extends JpaRepository<SkyCode, Long> {
}
