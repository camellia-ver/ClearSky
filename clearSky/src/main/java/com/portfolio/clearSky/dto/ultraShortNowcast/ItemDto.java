package com.portfolio.clearSky.dto.ultraShortNowcast;

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
    private String obsrValue;
}
