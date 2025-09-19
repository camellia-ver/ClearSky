package com.portfolio.clearSky.model.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PtyCodeValue {
    // ULTRA_SHORT_TERM
    NONE_ULTRA(PtyType.ULTRA_SHORT_TERM, "0", "없음"),
    RAIN_ULTRA(PtyType.ULTRA_SHORT_TERM, "1", "비"),
    RAIN_SNOW_ULTRA(PtyType.ULTRA_SHORT_TERM, "2", "비/눈"),
    SNOW_ULTRA(PtyType.ULTRA_SHORT_TERM, "3", "눈"),
    DRIZZLE_ULTRA(PtyType.ULTRA_SHORT_TERM, "5", "빗방울"),
    DRIZZLE_SNOW_ULTRA(PtyType.ULTRA_SHORT_TERM, "6", "빗방울눈날림"),
    SNOW_FLURRY_ULTRA(PtyType.ULTRA_SHORT_TERM, "7", "눈날림"),

    // SHORT_TERM
    NONE_SHORT(PtyType.SHORT_TERM, "0", "없음"),
    RAIN_SHORT(PtyType.SHORT_TERM, "1", "비"),
    RAIN_SNOW_SHORT(PtyType.SHORT_TERM, "2", "비/눈"),
    SNOW_SHORT(PtyType.SHORT_TERM, "3", "눈"),
    SHOWER_SHORT(PtyType.SHORT_TERM, "4", "소나기");

    private final PtyType type;
    private final String codeValue;
    private final String codeName;
}
