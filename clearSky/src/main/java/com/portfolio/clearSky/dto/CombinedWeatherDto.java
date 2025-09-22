package com.portfolio.clearSky.dto;

import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data
public class CombinedWeatherDto {
    // 1. 기온 (T1H)
    private Double temperature;
    // 2. 습도 (REH)
    private Integer humidity;
    // 3. 강수 형태 (PTY)
    private String precipitationType;
    // 4. 강수량 (RN1)
    private Double precipitationAmount; // RN1 값 사용
    // 5. 풍속 (WSD)
    private Double windSpeed;
    // 6. 동서바람성분 (UUU)
    private Double eastWestWindComponent;
    // 7. 남북바람성분 (VVV)
    private Double northSouthWindComponent;
    // 8. 풍향 (VEC)
    private Double windDirectionDegrees; // 각도(deg) 값

    private static final Map<Integer, String> PTY_CODE_MAP;

    static {
        Map<Integer, String> map = new HashMap<>();
        map.put(0, "없음");
        map.put(1, "비");
        map.put(2, "비/눈");
        map.put(3, "눈");
        map.put(4, "소나기");

        PTY_CODE_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * PTY 코드(0~4)를 문자열로 변환합니다.
     */
    public String getPrecipitationTypeString(int ptyCode) {
        return PTY_CODE_MAP.getOrDefault(ptyCode, "알 수 없음");
    }
}
