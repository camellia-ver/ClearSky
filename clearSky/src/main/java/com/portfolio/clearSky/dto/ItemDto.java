package com.portfolio.clearSky.dto;

import lombok.Data;

@Data
public class ItemDto {
    private String baseDate;
    private String baseTime;
    private String category;
    private int nx;
    private int ny;
    private String obsrValue;

    private String fcstDate;
    private String fcstTime;
    private String fcstValue;

    private String categoryName;
}
