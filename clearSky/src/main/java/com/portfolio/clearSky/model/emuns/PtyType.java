package com.portfolio.clearSky.model.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PtyType {
    ULTRA_SHORT_TERM("초단기","Ultra Short-Term"),
    SHORT_TERM("단기","Short-Term");

    private final String ultraShortTerm;
    private final String shortTerm;
}
