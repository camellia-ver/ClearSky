package com.portfolio.clearSky.common.cache;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class WeatherCacheKey {
    private final String baseDate;
    private final String baseTime;
    private final String type;
    private final Integer gridX;
    private final Integer gridY;
}
