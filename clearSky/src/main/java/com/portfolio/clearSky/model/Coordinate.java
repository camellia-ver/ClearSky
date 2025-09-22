package com.portfolio.clearSky.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    /**
     * 도/분/초 데이터를 십진수 형태의 Double로 변환합니다.
     * @return 십진수 형태의 좌표 (Double)
     */
    public Double toDecimal(){
        if (this.deg == null){
            return null;
        }

        // 1. deg를 BigDecimal로 변환
        BigDecimal decimal = new BigDecimal(this.deg);

        // 2. min을 60으로 나누어 더합니다.
        if (this.min != null){
            BigDecimal minPart = new BigDecimal(this.min)
                    .divide(new BigDecimal(60), 10, RoundingMode.HALF_UP);
            decimal = decimal.add(minPart);
        }

        // 3. sec을 3600으로 나누어 더합니다.
        if (this.sec != null) {
            BigDecimal secPart = this.sec
                    .divide(new BigDecimal(3600), 10, RoundingMode.HALF_UP);
            decimal = decimal.add(secPart);
        }

        // 4. sec100을 360000으로 나누어 더합니다.
        if (this.sec100 != null) {
            BigDecimal sec100Part = this.sec100
                    .divide(new BigDecimal(360000), 10, RoundingMode.HALF_UP);
            decimal = decimal.add(sec100Part);
        }

        // 최종 결과를 Double로 반환
        return decimal.doubleValue();
    }
}
