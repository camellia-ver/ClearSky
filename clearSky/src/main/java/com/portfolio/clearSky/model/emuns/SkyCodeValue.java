package com.portfolio.clearSky.model.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SkyCodeValue {
    CLEAR("1", "맑음"),
    PARTLY_CLOUDY("3", "구름많음"),
    CLOUDY("4", "흐림");

    private final String codeValue;
    private final String codeName;
}
