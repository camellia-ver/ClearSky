package com.portfolio.clearSky.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:application-API-URL.properties")
public class WeatherDataLoader {
    @Value("${ultra.short.nowcast.url}")
    private String ultraShortNowcastApiUrl;
    @Value("${ultra.short.forecast.url}")
    private String ultraShortForecastApiUrl;
    @Value("${open.data.api.key}")
    private String serviceKey;

    private final static String DATA_TYPE = "JSON";

    //초단기실황조회 API 요청
    public void fetchUltraShortNowcast(){
        String baseDate = getBaseDate();
        String baseTime = buildBaseTime(this::getNowcastBaseTime);
        // TODO: API 호출 로직 추가 예정
    }

    // 초단기예보조회 API 요청 함수
    public void fetchUltraShortForecast(){
        String baseDate = getBaseDate();
        String baseTime = buildBaseTime(this::getForecastBaseTime);
        // TODO: API 호출 로직 추가 예정
    }

    // 날짜 yyyyMMdd
    private String getBaseDate() {
        LocalDate today = LocalDate.now();
        return today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String buildBaseTime(Supplier<String> timeSupplier){
        return timeSupplier.get();
    }

    // 초단기실황 기준 시각 (매시각 정시 기준, 10분 전이면 이전 시각 사용)
    private String getNowcastBaseTime() {
        LocalTime now = LocalTime.now();
        LocalTime baseTime = now.getMinute() < 10
                ? now.minusHours(1).withMinute(0)
                : now.withMinute(0);

        return baseTime.format(DateTimeFormatter.ofPattern("HHmm"));
    }

    // 초단기예보 기준 시각 (매시각 30분 발표, 45분 전이면 이전 시각 사용)
    private String getForecastBaseTime() {
        LocalTime now = LocalTime.now();
        LocalTime baseTime = now.getMinute() < 45
                ? now.minusHours(1).withMinute(30)
                : now.withMinute(30);

        return baseTime.format(DateTimeFormatter.ofPattern("HHmm"));
    }
}
