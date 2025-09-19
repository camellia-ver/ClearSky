package com.portfolio.clearSky.service;

import com.portfolio.clearSky.model.AdministrativeBoundary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
@PropertySource("classpath:application-Open-Data-API.properties")
public class WeatherDataLoader {
    @Value("${ultra.short.nowcast.url}")
    private String ultraShortNowcastApiUrl;
    @Value("${ultra.short.forecast.url}")
    private String ultraShortForecastApiUrl;
    @Value("${open.data.api.key}")
    private String serviceKey;

    private final static String DATA_TYPE = "JSON";

    private final AdministrativeBoundaryService administrativeBoundaryService;

    //초단기실황조회 API 요청
    public void fetchUltraShortNowcast(){
        String baseDate = getBaseDate();
        String baseTime = buildBaseTime(this::getNowcastBaseTime);

        List<AdministrativeBoundary> abList = administrativeBoundaryService.getAllLocations();

        for (AdministrativeBoundary ab: abList) {
            String url = buildUrl(ultraShortNowcastApiUrl, baseDate, baseTime, ab);
        }
    }

    @Scheduled(cron = "0 43 16 * * *")
    // 초단기예보조회 API 요청 함수
    public void fetchUltraShortForecast(){
        String baseDate = getBaseDate();
        String baseTime = buildBaseTime(this::getForecastBaseTime);

        List<AdministrativeBoundary> abList = administrativeBoundaryService.getAllLocations();

        for (AdministrativeBoundary ab: abList) {
            String url = buildUrl(ultraShortForecastApiUrl, baseDate, baseTime, ab);
            System.out.println(url);
        }
    }

    private String buildUrl(String baseUrl, String baseDate, String baseTime, AdministrativeBoundary ab){
        return baseUrl + "?serviceKey=" + getEncodingServiceKey()
                + "&dataType=" + DATA_TYPE
                + "&base_date=" + baseDate
                + "&base_time=" + baseTime
                + "&nx=" + ab.getGridX()
                + "&ny=" + ab.getGridY();
    }

    private String getEncodingServiceKey(){
        return URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);
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