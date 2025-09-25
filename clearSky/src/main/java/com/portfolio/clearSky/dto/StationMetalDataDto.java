package com.portfolio.clearSky.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class StationMetalDataDto {
    private final String stationName;
    private final Map<String, String> itemData;
}
