package com.portfolio.clearSky.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.portfolio.clearSky.dto.ItemDto;
import com.portfolio.clearSky.dto.ResponseWrapper;
import com.portfolio.clearSky.model.AdministrativeBoundary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
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

    /**
     * WebFlux 환경에서 비동기 테스트용 함수
     */
    public void testNowcastApiAsync(AdministrativeBoundary ab) {
        String baseDate = getBaseDate();
        String baseTime = getNowcastBaseTime();
        String url = buildUrl(ultraShortNowcastApiUrl, baseDate, baseTime, ab).toString();
        log.info("🔍 Testing Weather API (async) with URL: {}", url);

        test(url, ab.getId())
                .subscribe(
                        res -> log.info("✅ API success, raw response for abId={}: {}", ab.getId(), res),
                        err -> log.error("❌ API call failed for abId={}", ab.getId(), err),
                        () -> log.info("✔️ Completed API call for abId={}", ab.getId())
                );
//        fetchDataFromApi(url, ab.getId())
//                .subscribe(
//                        res -> log.info("✅ API success, raw response for abId={}: {}", ab.getId(), res),
//                        err -> log.error("❌ API call failed for abId={}", ab.getId(), err),
//                        () -> log.info("✔️ Completed API call for abId={}", ab.getId())
//                );
    }

    /**
     * 특정 지역 초단기 실황 가져오기
     */
//    public Mono<List<ItemDto>> getNowcastForLocation(AdministrativeBoundary ab){
//        String baseDate = getBaseDate();
//        String baseTime = getNowcastBaseTime();
//        String url = buildUrl(ultraShortNowcastApiUrl, baseDate, baseTime, ab).toString();
//        log.info(url);
//
//        return fetchDataFromApi(url, ab.getId());
//    }

    /**
     * 특정 지역 초단기 예보 가져오기
     */
//    public Mono<List<ItemDto>> getForecastForLocation(AdministrativeBoundary ab) {
//        String baseDate = getBaseDate();
//        String baseTime = getForecastBaseTime();
//        String url = buildUrl(ultraShortForecastApiUrl, baseDate, baseTime, ab).toString();
//
//        return fetchDataFromApi(url, ab.getId());
//    }

    private Mono<String> test(String url, Long abId){
         return webClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_XML)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(xml -> log.info("Raw API XML for abId={}: {}", abId, xml));
    }

    /**
     * API 호출 후 ItemDto 리스트 반환
     */
    public Mono<List<ItemDto>> fetchDataFromApi(String url, Long abId) {
        return webClient.get()
                .uri(url)
                .header(MediaType.APPLICATION_XML_VALUE)
                .retrieve()
                .bodyToMono(String.class) // 먼저 문자열로 받기
                .map(xml -> {
                    try {
                        XmlMapper xmlMapper = new XmlMapper();
                        ResponseWrapper wrapper = xmlMapper.readValue(xml, ResponseWrapper.class);

                        if (wrapper.getBody() != null
                                && wrapper.getBody().getItems() != null
                                && wrapper.getBody().getItems().getItem() != null) {
                            return wrapper.getBody().getItems().getItem();
                        } else {
                            log.warn("⚠️ No items returned for abId={}", abId);
                            return Collections.<ItemDto>emptyList();
                        }
                    } catch (Exception e) {
                        log.error("❌ XML parsing failed for abId={}", abId, e);
                        return Collections.<ItemDto>emptyList();
                    }
                });
    }

    private URI buildUrl(String baseUrl, String baseDate, String baseTime, AdministrativeBoundary ab){
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("serviceKey", serviceKey)
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
