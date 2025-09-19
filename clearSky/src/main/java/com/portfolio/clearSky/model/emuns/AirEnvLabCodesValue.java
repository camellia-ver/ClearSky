package com.portfolio.clearSky.model.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AirEnvLabCodesValue {
    SUDO_KWON("1", "수도권"),
    BAEKRYEONG_DO("2", "백령도"),
    HONAM_KWON("3", "호남권"),
    JUNGBU_KWON("4", "중부권"),
    JEJU_DO("5", "제주도"),
    YEONGNAM_KWON("6", "영남권"),
    GYEONGGI_KWON("7", "경기권"),
    CHUNGCHUNG_KWON("8", "충청권"),
    JEONBUK_KWON("9", "전북권"),
    GANGWON_KWON("10", "강원권"),
    CHUNGBUK_KWON("11", "충북권");

    private final String code;
    private final String koreanName;
}
