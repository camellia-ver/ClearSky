package com.portfolio.clearSky.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "forecast_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ForecastCodes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String forecast_type;

    @Column(nullable = false, length = 5)
    private String code_value;

    @Column(nullable = false, length = 20)
    private String code_name;

    @Column(nullable = false, length = 10)
    private String unit;
}
