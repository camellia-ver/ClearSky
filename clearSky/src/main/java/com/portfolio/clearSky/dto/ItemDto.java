package com.portfolio.clearSky.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ItemDto {
    private String baseDate;
    private String baseTime;
    private String category;
    private int nx;
    private int ny;
    private String obsrValue;
}
