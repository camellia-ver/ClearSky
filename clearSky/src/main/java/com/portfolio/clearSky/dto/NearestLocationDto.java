package com.portfolio.clearSky.dto;

import lombok.Data;

@Data
public class NearestLocationDto {
    private final Integer gridX;
    private final Integer gridY;
    private final Double distanceKm;
}
