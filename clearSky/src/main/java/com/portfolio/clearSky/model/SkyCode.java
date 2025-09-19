package com.portfolio.clearSky.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sky_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SkyCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 5)
    private String codeValue;

    @Column(nullable = false, length = 10)
    private String codeName;
}
