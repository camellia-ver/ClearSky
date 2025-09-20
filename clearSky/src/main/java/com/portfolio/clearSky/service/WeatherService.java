package com.portfolio.clearSky.service;

import com.portfolio.clearSky.dto.ItemDto;
import com.portfolio.clearSky.dto.RootDto;
import com.portfolio.clearSky.model.AdministrativeBoundary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

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
    private final AdministrativeBoundaryService administrativeBoundaryService;

    /**
     * 특정 지역 초단기 실황 가져오기
     */
    public Mono<List<ItemDto>> getNowcastForLocation(AdministrativeBoundary ab){
        String baseDate = getBaseDate();
        String baseTime = getNowcastBaseTime();
        String url = buildUrl(ultraShortNowcastApiUrl, baseDate, baseTime, ab).toString();

        return fetchDataFromApi(url, ab.getId());
    }

    /**
     * 특정 지역 초단기 예보 가져오기
     */
    public Mono<List<ItemDto>> getForecastForLocation(AdministrativeBoundary ab) {
        String baseDate = getBaseDate();
        String baseTime = getForecastBaseTime();
        String url = buildUrl(ultraShortForecastApiUrl, baseDate, baseTime, ab).toString();

        return fetchDataFromApi(url, ab.getId());
    }

    private Mono<List<ItemDto>> fetchDataFromApi(String url, Long abId){
        return webClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(RootDto.class)
                .timeout(Duration.ofSeconds(5))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .onRetryExhaustedThrow((spec, signal) ->
                                new RuntimeException("Failed after retries for ab: " + abId, signal.failure())))
                .map(rootDto -> {
                    if (rootDto == null || rootDto.getResponse() == null
                        || rootDto.getResponse().getBody() == null
                        || rootDto.getResponse().getBody().getItems() == null
                        || rootDto.getResponse().getBody().getItems().getItem() == null){
                        return List.<ItemDto>of();
                    }
                    return rootDto.getResponse().getBody().getItems().getItem();
                });
    }

    private URI buildUrl(String baseUrl, String baseDate, String baseTime, AdministrativeBoundary ab){
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("dataType", "JSON")
                .queryParam("numOfRows", 10)
                .queryParam("pageNo", 1)
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", ab.getGridX())
                .queryParam("ny", ab.getGridY())
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
