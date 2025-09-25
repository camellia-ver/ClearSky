package com.portfolio.clearSky.common.cache;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class CacheKey {
    private final String baseDate;
    private final String baseTime;
    private final String type;
    private final Integer gridX;
    private final Integer gridY;

    private final String stationcode;
    private final String itemcode;
}
