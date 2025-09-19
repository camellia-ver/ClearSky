package com.portfolio.clearSky.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "air_env_lab_search_codes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class AirEnvLabSearchCodes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String searchCode;

    @Column(nullable = false, length = 5)
    private String searchName;
}
