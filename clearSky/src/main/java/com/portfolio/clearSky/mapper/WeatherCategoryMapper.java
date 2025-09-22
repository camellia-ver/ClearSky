package com.portfolio.clearSky.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WeatherCategoryMapper {
    public static final Map<String, String> CATEGORY_MAP;

    static {
        Map<String, String> map = new HashMap<>();

        map.put("POP", "강수확률");
        map.put("T1H", "기온");
        map.put("RN1", "1시간 강수량");
        map.put("UUU", "동서바람성분");
        map.put("VVV", "남북바람성분");
        map.put("REH", "습도");
        map.put("PTY", "강수형태");
        map.put("VEC", "풍향");
        map.put("WSD", "풍속");
        map.put("SKY", "하늘상태");
        map.put("LGT", "낙뢰");

        CATEGORY_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * 카테고리 코드를 입력받아 사용자 친화적인 이름을 반환합니다.
     * 매핑되는 값이 없을 경우, 원본 코드(categoryCode)를 그대로 반환합니다.
     * @param categoryCode 공공데이터 카테고리 코드 (예: "T1H")
     * @return 변환된 이름 (예: "기온")
     */
    public static String getCategoryName(String categoryCode){
        return CATEGORY_MAP.getOrDefault(categoryCode, categoryCode);
    }

    public static final Map<String, String> UNIT_MAP;

    static {
        // 단위 매핑
        Map<String, String> unitMap = new HashMap<>();

        unitMap.put("POP", "%");
        unitMap.put("RN1", "mm");
        unitMap.put("UUU", "m/s");
        unitMap.put("VVV", "m/s");
        unitMap.put("REH", "%");
        unitMap.put("VEC", "deg");
        unitMap.put("WSD", "m/s");
        unitMap.put("LGT", "kA");

        UNIT_MAP = Collections.unmodifiableMap(unitMap);
    }

    /**
     * 카테고리 코드를 입력받아 해당 값의 단위를 반환합니다.
     * 매핑되는 단위가 없을 경우, 빈 문자열("")을 반환하여 값만 표시하도록 합니다.
     * @param categoryCode 공공데이터 카테고리 코드 (예: "T1H")
     * @return 해당 값의 단위 (예: "°C")
     */
    public static String getUnit(String categoryCode) {
        return UNIT_MAP.getOrDefault(categoryCode, "");
    }
}
