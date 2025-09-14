package com.portfolio.clearSky.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Coordinate {
    private int deg; // 도
    private int min; // 분
    private BigDecimal sec; // 초
    private BigDecimal sec100; // 초/100
}
