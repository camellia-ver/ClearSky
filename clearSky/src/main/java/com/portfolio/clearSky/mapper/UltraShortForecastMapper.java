package com.portfolio.clearSky.mapper;

import com.portfolio.clearSky.dto.ItemDto;
import com.portfolio.clearSky.model.AdministrativeBoundary;
import com.portfolio.clearSky.model.UltraShortForecast;
import com.portfolio.clearSky.model.emuns.ForecastCategory;
import com.portfolio.clearSky.model.emuns.WeatherCategory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class UltraShortForecastMapper {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

    public static UltraShortForecast toEntity(ItemDto dto, AdministrativeBoundary ab, String baseDateStr, String baseTimeStr) {
        return UltraShortForecast.builder()
                    .administrativeBoundary(ab)
                    .baseDate(LocalDate.parse(baseDateStr, DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .baseTime(LocalTime.parse(baseTimeStr, DateTimeFormatter.ofPattern("HHmm")))
                    .forecastDate(LocalDate.parse(dto.getFcstDate(), DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .forecastTime(LocalTime.parse(dto.getFcstTime(), DateTimeFormatter.ofPattern("HHmm")))
                    .category(ForecastCategory.valueOf(dto.getCategory()))
                    .forecastValue(Double.valueOf(dto.getFcstValue()))
                    .nx(dto.getNx())
                    .ny(dto.getNy())
                    .build();
    }
}
