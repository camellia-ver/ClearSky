package com.portfolio.clearSky.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BaseDateTimeDto {
    private final String baseDate; // yyyyMMdd 형식
    private final String baseTime; // HHmm 형식
}
