package com.portfolio.clearSky.model;

import com.portfolio.clearSky.model.emuns.PtyType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pty_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PtyCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PtyType type;

    @Column(nullable = false, length = 5)
    private String codeValue;

    @Column(nullable = false, length = 10)
    private String codeName;
}
