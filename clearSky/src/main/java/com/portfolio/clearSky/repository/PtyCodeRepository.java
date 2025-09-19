package com.portfolio.clearSky.repository;

import com.portfolio.clearSky.model.PtyCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PtyCodeRepository extends JpaRepository<PtyCode, Long> {
}
