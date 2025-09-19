package com.portfolio.clearSky.model.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AirEnvLabSearchCodesValue {
    LEAD("90303", "납"),
    NICKEL("90304", "니켈"),
    MANGANESE("90305", "망간"),
    ZINC("90314", "아연"),
    CALCIUM("90319", "칼슘"),
    POTASSIUM("90318", "칼륨"),
    SULFUR("90325", "황");

    private final String code;
    private final String name;
}
