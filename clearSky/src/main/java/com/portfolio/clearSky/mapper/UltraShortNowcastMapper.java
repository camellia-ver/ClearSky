package com.portfolio.clearSky.mapper;

import com.portfolio.clearSky.dto.ItemDto;
import com.portfolio.clearSky.model.AdministrativeBoundary;
import com.portfolio.clearSky.model.UltraShortNowcast;
import com.portfolio.clearSky.model.emuns.WeatherCategory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class UltraShortNowcastMapper {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

    public static UltraShortNowcast toEntity(ItemDto dto, AdministrativeBoundary boundary) {
        return UltraShortNowcast.builder()
                .baseDate(LocalDate.parse(dto.getBaseDate(), DATE_FORMATTER))
                .baseTime(LocalTime.parse(dto.getBaseTime(), TIME_FORMATTER))
                .nx(dto.getNx())
                .ny(dto.getNy())
                .category(WeatherCategory.valueOf(dto.getCategory()))
                .observedValue(Double.valueOf(dto.getObsrValue()))
                .administrativeBoundary(boundary)
                .build();
    }
}
