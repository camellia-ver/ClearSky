package com.portfolio.clearSky.model.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AirEnvLabTimeCategory {
    RH02("RH02","2시간이동평균"), // 기본값
    RH24("RH24","24시간이동평균");

    private final String code;
    private final String name;
}
