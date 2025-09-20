package com.portfolio.clearSky.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemDto {
    private String baseDate;
    private String baseTime;
    private String category;
    private int nx;
    private int ny;

    // Nowcast
    private String obsrValue;

    // Forecast
    private String fcstDate;
    private String fcstTime;
    private String fcstValue;
}
