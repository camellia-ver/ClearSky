package com.portfolio.clearSky.model;

import com.portfolio.clearSky.model.emuns.WeatherCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "ultra_short_nowcast")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class UltraShortNowcast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate baseDate; // ex) 20210628
    private LocalTime baseTime; // ex) 0600

    private Integer nx;
    private Integer ny;

    @Enumerated(EnumType.STRING)
    private WeatherCategory category;

    private Double observedValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrative_boundary_id")
    private AdministrativeBoundary administrativeBoundary;
}
