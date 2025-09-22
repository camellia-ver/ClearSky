package com.portfolio.clearSky.dto;

import lombok.*;

@Data
@ToString
public class LocationDTO {
    private String full_address; // 지역 이름
    private Double lat;  // 위도
    private Double lng;  // 경도
}
