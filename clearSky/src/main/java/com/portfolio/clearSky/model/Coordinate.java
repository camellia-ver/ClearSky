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
    private Integer deg; // 시
    private Integer min; // 분
    private BigDecimal sec; // 초
    private BigDecimal sec100; // 초/100
}
