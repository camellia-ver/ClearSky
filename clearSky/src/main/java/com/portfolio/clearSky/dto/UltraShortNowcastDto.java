package com.portfolio.clearSky.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UltraShortNowcastDto {
    private String baseDate;   // "20250919"
    private String baseTime;   // "2200"
    private String category;   // "PTY"
    private Integer nx;
    private Integer ny;
    private String observedValue;  // "1"
}
