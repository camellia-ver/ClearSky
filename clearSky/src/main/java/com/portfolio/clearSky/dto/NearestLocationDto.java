package com.portfolio.clearSky.dto;

import lombok.Data;

@Data
public class NearestLocationDto {
    private final Integer gridX;
    private final Integer gridY;
    private final Double distanceKm;

    private final String admLevel1; // 시/도
    private final String admLevel2; // 시/군/구
    private final String admLevel3; // 읍/면/동

    public String getFullAddress() {
        String level2 = admLevel2 != null ? admLevel2 : "";
        String level3 = admLevel3 != null ? admLevel3 : "";
        return String.format("%s %s %s", admLevel1, level2, level3).trim();
    }
}
