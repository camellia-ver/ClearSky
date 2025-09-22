package com.portfolio.clearSky.dto;

import lombok.Data;

@Data
public class WeatherDisplayDto {
    // ItemDto 필드 (날짜, 시간, 위치 정보)
    private String baseDate;
    private String baseTime;
    private int nx;
    private int ny;

    // 카테고리 정보 (원본 코드와 변환된 이름)
    private String category; // 원본 공공데이터 코드 (예: T1H)
    private String categoryName; // 변환된 사용자 친화적 이름 (예: 기온)

    // 실황 값 및 예보 값
    private String obsrValue; // 실황 값
    private String fcstDate;
    private String fcstTime;
    private String fcstValue; // 예측 값
}
