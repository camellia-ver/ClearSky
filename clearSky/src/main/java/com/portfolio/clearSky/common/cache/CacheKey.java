package com.portfolio.clearSky.common.cache;

import com.portfolio.clearSky.model.AdministrativeBoundary;
import com.portfolio.clearSky.model.emuns.ForecastType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class CacheKey {
    private final String baseDate;
    private final String baseTime;
    private final ForecastType type;
    private final Integer gridX;
    private final Integer gridY;
}
