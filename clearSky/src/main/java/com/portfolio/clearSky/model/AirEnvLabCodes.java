package com.portfolio.clearSky.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "air_env_lab_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AirEnvLabCodes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5)
    private String labCode;

    @Column(nullable = false, length = 10)
    private String labName;
}
