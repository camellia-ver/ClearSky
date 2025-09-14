package com.portfolio.clearSky.model;

import com.portfolio.clearSky.model.enums.RegionCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "administrative_boundary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AdministrativeBoundary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RegionCategory category;

    @Column(nullable = false, length = 10)
    private String admCode; // 행정구역코드

    @Column(nullable = false, length = 50)
    private String admLevel1; // 1단계
    @Column(length = 50)
    private String admLevel2; // 2단계
    @Column(length = 50)
    private String admLevel3; // 3단계

    private BigDecimal gridX;
    private BigDecimal gridY;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "deg", column = @Column(name = "lon_deg")),
            @AttributeOverride(name = "min", column = @Column(name = "lon_min")),
            @AttributeOverride(name = "sec", column = @Column(name = "lon_sec")),
            @AttributeOverride(name = "sec100", column = @Column(name = "lon_sec100"))
    })
    private Coordinate longitude;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "deg", column = @Column(name = "lat_deg")),
            @AttributeOverride(name = "min", column = @Column(name = "lat_min")),
            @AttributeOverride(name = "sec", column = @Column(name = "lat_sec")),
            @AttributeOverride(name = "sec100", column = @Column(name = "lat_sec100"))
    })
    private Coordinate latitude;

    private LocalDate locUpdate;
}
