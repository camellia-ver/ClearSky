package com.portfolio.clearSky.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.clearSky.dto.ultraShortNowcast.ItemDto;
import com.portfolio.clearSky.dto.ultraShortNowcast.RootDto;
import com.portfolio.clearSky.mapper.UltraShortNowcastMapper;
import com.portfolio.clearSky.model.AdministrativeBoundary;
import com.portfolio.clearSky.model.UltraShortNowcast;
import com.portfolio.clearSky.repository.UltraShortForecastRepository;
import com.portfolio.clearSky.repository.UltraShortNowcastRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

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

    private final static int BATCH_SIZE = 1000;

    private final WebClient webClient;
    private final AdministrativeBoundaryService administrativeBoundaryService;
    private final UltraShortNowcastRepository ultraShortNowcastRepository;
    private final UltraShortForecastRepository ultraShortForecastRepository;

    //초단기실황조회 API 요청
    @Scheduled(cron = "0 23 19 * * *")
    public void fetchUltraShortNowcast() {
        String baseDate = getBaseDate();
        String baseTime = buildBaseTime(this::getNowcastBaseTime);

        List<AdministrativeBoundary> abList = administrativeBoundaryService.getAllLocations();

        for (AdministrativeBoundary ab : abList) {
            URI url = buildUrl(ultraShortNowcastApiUrl, baseDate, baseTime, ab);

            try {
                RootDto rootDto = webClient.get()
                        .uri(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(RootDto.class)
                        .block(); // 동기 처리

                if (rootDto == null || rootDto.getResponse() == null) {
                    log.warn("No response for AdministrativeBoundary: {}", ab.getId());
                    continue;
                }

                String resultCode = rootDto.getResponse().getHeader().getResultCode();
                log.info("Result Code: {} for {}", resultCode, ab.getId());

                List<ItemDto> items = rootDto.getResponse().getBody().getItems().getItem();

                if (items == null || items.isEmpty()) {
                    log.warn("No items found for {}", ab.getId());
                    continue;
                }

                // DB 저장
                saveAllFromDtos(items, ab);

                items.forEach(item ->
                        log.info("Saved Nowcast => {} : {} (ab: {})",
                                item.getCategory(), item.getObsrValue(), ab.getId()));

            } catch (Exception e) {
                // JSON 변환 실패나 XML 반환 등 모든 예외를 잡아서 로그만 남기고 다음으로 넘어가기
                log.warn("Failed to parse JSON for AdministrativeBoundary: {}. Skipping. Error: {}",
                        ab.getId(), e.getMessage());
            }
        }
    }

    // 초단기예보조회 API 요청 함수
    @Scheduled(cron = "0 43 16 * * *")
    public void fetchUltraShortForecast(){
        String baseDate = getBaseDate();
        String baseTime = buildBaseTime(this::getForecastBaseTime);

        List<AdministrativeBoundary> abList = administrativeBoundaryService.getAllLocations();
//
//        for (AdministrativeBoundary ab: abList) {
//            String url = buildUrl(ultraShortForecastApiUrl, baseDate, baseTime, ab);
//        }
    }

    // DB 저장 메서드 수정
    public void saveFromDto(ItemDto dto, AdministrativeBoundary boundary) {
        UltraShortNowcast entity = UltraShortNowcastMapper.toEntity(dto, boundary);
        ultraShortNowcastRepository.save(entity);
    }

    public void saveAllFromDtos(List<ItemDto> dtos, AdministrativeBoundary boundary) {
        List<UltraShortNowcast> entities = dtos.stream()
                .map(dto -> UltraShortNowcastMapper.toEntity(dto, boundary))
                .toList();
        ultraShortNowcastRepository.saveAll(entities);
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
