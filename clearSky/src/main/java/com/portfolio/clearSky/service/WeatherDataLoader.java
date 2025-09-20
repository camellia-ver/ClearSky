package com.portfolio.clearSky.service;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
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
    public void fetchUltraShortNowcastAsync() {
        String baseDate = getBaseDate();
        String baseTime = buildBaseTime(this::getNowcastBaseTime);

        List<AdministrativeBoundary> abList = administrativeBoundaryService.getAllLocations();
        int total = abList.size();
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Flux.fromIterable(abList)
                .parallel() // 병렬 처리
                .runOn(Schedulers.boundedElastic()) // I/O 작업에 적합
                .flatMap(ab -> {
                    URI url = buildUrl(ultraShortNowcastApiUrl, baseDate, baseTime, ab);

                    return webClient.get()
                            .uri(url)
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(RootDto.class)
                            .doOnNext(rootDto -> {
                                processedCount.incrementAndGet();

                                if (rootDto == null) {
                                    log.warn("Empty response for AdministrativeBoundary: {}. Skipping.", ab.getId());
                                    failCount.incrementAndGet();
                                    return;
                                }

                                if (rootDto.getResponse() == null) {
                                    log.warn("Non-JSON or invalid response received for AdministrativeBoundary: {}. Skipping.", ab.getId());
                                    failCount.incrementAndGet();
                                    return;
                                }

                                List<ItemDto> items = rootDto.getResponse().getBody().getItems().getItem();
                                if (items == null || items.isEmpty()) {
                                    log.warn("No items found for {}", ab.getId());
                                    failCount.incrementAndGet();
                                    return;
                                }

                                // 배치 저장
                                saveAllFromDtosBatch(items, ab);
                                successCount.incrementAndGet();

                                items.forEach(item ->
                                        log.info("Saved Nowcast => {} : {} (ab: {})",
                                                item.getCategory(), item.getObsrValue(), ab.getId()));

                                log.info("Progress: {}/{} ({}%) processed, Success: {}, Fail: {}",
                                        processedCount.get(),
                                        total,
                                        processedCount.get() * 100 / total,
                                        successCount.get(),
                                        failCount.get());
                            })
                            .onErrorResume(e -> {
                                processedCount.incrementAndGet();
                                failCount.incrementAndGet();
                                log.warn("Failed to fetch for {}: {}", ab.getId(), e.getMessage());
                                return Mono.empty();
                            });
                })
                .sequential()
                .blockLast(); // 모든 요청 완료 대기
    }

    // 배치 단위로 DB 저장
    @Transactional
    public void saveAllFromDtosBatch(List<ItemDto> dtos, AdministrativeBoundary boundary) {
        List<UltraShortNowcast> entities = dtos.stream()
                .map(dto -> UltraShortNowcastMapper.toEntity(dto, boundary))
                .toList();

        for (int i = 0; i < entities.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, entities.size());
            ultraShortNowcastRepository.saveAll(entities.subList(i, end));
            ultraShortNowcastRepository.flush(); // 배치별 commit
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
