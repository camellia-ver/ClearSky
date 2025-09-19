package com.portfolio.clearSky.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "ultra_short_forecast")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class UltraShortForecast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate baseDate; // ex) 20210628
    private LocalTime baseTime; // ex) 1200

    private Integer nx;
    private Integer ny;

    @Enumerated(EnumType.STRING)
    private String category;

    private LocalDate forecastDate;
    private LocalTime forecastTime;

    private Double forecastValue;
}
