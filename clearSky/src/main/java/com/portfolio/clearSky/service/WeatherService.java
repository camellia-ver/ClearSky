package com.portfolio.clearSky.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.portfolio.clearSky.common.cache.CacheKey;
import com.portfolio.clearSky.dto.BaseDateTimeDto;
import com.portfolio.clearSky.dto.ItemDto;
import com.portfolio.clearSky.dto.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
        BaseDateTimeDto baseDateTime = getNowcastBaseDateTime();
        String baseDate = baseDateTime.getBaseDate();
        String baseTime = baseDateTime.getBaseTime();

        CacheKey key = new CacheKey(baseDate, baseTime, "NOWCAST", gridX, gridY, null, null);
        return getOrFetch(key);
    }


    // 초단기 예보
    public Mono<List<ItemDto>> getForecastForLocation(Integer gridX, Integer gridY) {
        BaseDateTimeDto baseDateTime = getForecastBaseDateTime();
        String baseDate = baseDateTime.getBaseDate();
        String baseTime = baseDateTime.getBaseTime();

        CacheKey key = new CacheKey(baseDate, baseTime, "FORECAST", gridX, gridY, null, null);
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
        if (Objects.equals(key.getType(), "NOWCAST")) {
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
                .queryParam("numOfRows", 100)
                .queryParam("pageNo", 1)
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", gridX)
                .queryParam("ny", gridY)
                .build(true)
                .toUri();
    }

    private String buildBaseTime(Supplier<String> timeSupplier){
        return timeSupplier.get();
    }

    private BaseDateTimeDto getNowcastBaseDateTime(){
        LocalDateTime now = LocalDateTime.now();

        // 1. 발표 기준 시각 계산 (매시각 30분 발표, 45분 전이면 이전 시각 사용)
        LocalDateTime baseDateTime = now.getMinute() < 45
                ? now.minusHours(1).withMinute(30)
                : now.withMinute(30);

        // 2. 날짜와 시간 추출
        String baseDate = baseDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = baseDateTime.format(DateTimeFormatter.ofPattern("HHmm"));

        return new BaseDateTimeDto(baseDate, baseTime);
    }

    private BaseDateTimeDto getForecastBaseDateTime() {
        // 단기 예보 발표 시각 리스트 (HH)
        final List<Integer> times = List.of(2, 5, 8, 11, 14, 17, 20, 23);

        LocalDateTime now = LocalDateTime.now();

        // 1. 발표 기준 시각 찾기
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();

        // 가장 가까운 과거 발표 시각 (HH) 찾기
        int baseHour = times.stream()
                .filter(t -> t <= currentHour) // 현재 시각보다 같거나 작은 시각만 필터링
                .max(Integer::compare)
                .orElseGet(() ->
                        // 오늘 발표 시간이 없으면 (새벽 02시 이전), 어제 23시를 사용
                        23
                );

        // 2. baseTime 설정
        // 단기 예보는 발표 시각의 '00분'에 발표되며, 보통 40분 이후에 데이터가 확정된다고 가정
        LocalTime baseTime = LocalTime.of(baseHour, 0);

        // 3. baseDate 설정
        LocalDate baseDate = now.toLocalDate();

        // 4. 자정 처리: baseHour가 23이고 현재 시각이 00시 ~ 01시 40분 사이인 경우 (어제 발표 시각을 사용)
        if (baseHour == 23 && currentHour < 2) {
            baseDate = baseDate.minusDays(1);
        }

        return new BaseDateTimeDto(
                baseDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                baseTime.format(DateTimeFormatter.ofPattern("HHmm"))
        );
    }
}
