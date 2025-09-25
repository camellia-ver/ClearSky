package com.portfolio.clearSky.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MetalDataResponse {
    private final String date;
    private final String stationName;
    private final String itemName;
    private final String itemValue;
}
