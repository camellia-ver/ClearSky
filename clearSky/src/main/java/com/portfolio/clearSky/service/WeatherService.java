package com.portfolio.clearSky.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.portfolio.clearSky.common.cache.CacheKey;
import com.portfolio.clearSky.dto.ItemDto;
import com.portfolio.clearSky.dto.ResponseWrapper;
import com.portfolio.clearSky.model.emuns.ForecastType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
@PropertySource("classpath:application-Open-Data-API.properties")
public class WeatherService {
    @Value("${ultra.short.nowcast.url}")
    private String ultraShortNowcastApiUrl;
    @Value("${ultra.short.forecast.url}")
    private String ultraShortForecastApiUrl;
    @Value("${open.data.api.key}")
    private String serviceKey;

    private final WebClient webClient;
    private final AsyncLoadingCache<CacheKey, List<ItemDto>> cache = Caffeine.newBuilder()
            .expireAfterWrite(40, TimeUnit.HOURS)
            .maximumSize(1000)
            .buildAsync((key, executor) -> fetchDataForKey(key).toFuture());

    // 초단기 실황
    public Mono<List<ItemDto>> getNowcastForLocation(Integer gridX, Integer gridY) {
        String baseDate = getBaseDate();
        String baseTime = getNowcastBaseTime();

        CacheKey key = new CacheKey(baseDate, baseTime, ForecastType.NOWCAST, gridX, gridY);
        return getOrFetch(key);
    }


    // 초단기 예보
    public Mono<List<ItemDto>> getForecastForLocation(Integer gridX, Integer gridY) {
        String baseDate = getBaseDate();
        String baseTime = getForecastBaseTime();

        CacheKey key = new CacheKey(baseDate, baseTime, ForecastType.FORECAST, gridX, gridY);
        return getOrFetch(key);
    }

    private Mono<List<ItemDto>> getOrFetch(CacheKey key) {
        return Mono.fromFuture(() -> cache.get(key));
    }

    private Mono<List<ItemDto>> fetchDataForKey(CacheKey key) {
        String url = buildUrlForKey(key).toString();
        return fetchDataFromApi(url);
    }

    private URI buildUrlForKey(CacheKey key) {
        if (key.getType() == ForecastType.NOWCAST) {
            return buildUrl(ultraShortNowcastApiUrl, key.getBaseDate(), key.getBaseTime(), key.getGridX(), key.getGridY());
        } else {
            return buildUrl(ultraShortForecastApiUrl, key.getBaseDate(), key.getBaseTime(), key.getGridX(), key.getGridY());
        }
    }

    private Mono<List<ItemDto>> fetchDataFromApi(String url) {
        return webClient.get()
                .uri(url)
                .header("Content-Type", "application/xml")
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseXml);
    }

    private List<ItemDto> parseXml(String xml) {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            ResponseWrapper wrapper = xmlMapper.readValue(xml, ResponseWrapper.class);

            if (wrapper.getBody() != null && wrapper.getBody().getItems() != null
                    && wrapper.getBody().getItems().getItem() != null) {
                return wrapper.getBody().getItems().getItem();
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("XML 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private URI buildUrl(String baseUrl, String baseDate, String baseTime, Integer gridX, Integer gridY){
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", 10)
                .queryParam("pageNo", 1)
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", gridX)
                .queryParam("ny", gridY)
                .build(true)
                .toUri();
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
