package com.portfolio.clearSky.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.portfolio.clearSky.common.cache.CacheKey;
import com.portfolio.clearSky.dto.ItemDto;
import com.portfolio.clearSky.dto.MetalDataResponse;
import com.portfolio.clearSky.dto.ResponseWrapper;
import com.portfolio.clearSky.mapper.MetalDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@PropertySource("classpath:application-Open-Data-API.properties")
public class MetalMeasuringService {
    @Value("${metal.measuring.url}")
    private String baseUrl;

    @Value("${open.data.api.key}")
    private String serviceKey;

    private final WebClient webClient;
    private final MetalDataMapper metalDataMapper;

    private final AsyncLoadingCache<CacheKey, List<ItemDto>> cache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.HOURS)
            .maximumSize(1000)
            .buildAsync((key, executor) -> fetchDataForKey(key).toFuture());

    public Mono<List<MetalDataResponse>> getMetalDataForLocation(String stationcode, String itemcode) {
        CacheKey key = new CacheKey(
                null, // baseDate (날씨 API에서만 사용)
                null, // baseTime (날씨 API에서만 사용)
                "METAL", // 새로운 API를 구별할 고유 타입
                null, // gridX (날씨 API에서만 사용)
                null, // gridY (날씨 API에서만 사용)
                stationcode,
                itemcode
        );

        return getOrFetch(key)
                .map(metalDataMapper::map);
    }

    private Mono<List<ItemDto>> getOrFetch(CacheKey key) {
        return Mono.fromFuture(() -> cache.get(key));
    }

    private Mono<List<ItemDto>> fetchDataForKey(CacheKey key){
        String stationcode = key.getStationcode();
        String itemcode = key.getItemcode();

        String url = buildUrl(stationcode, itemcode).toString();

        return fetchDataFromApi(url);
    }

    private Mono<List<ItemDto>> fetchDataFromApi(String url){
        return webClient.get()
                .uri(url)
                .header("Content-Type", "application/xml")
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseXml);
    }

    private List<ItemDto> parseXml(String xml){
        try{
            XmlMapper xmlMapper = new XmlMapper();
            ResponseWrapper wrapper = xmlMapper.readValue(xml, ResponseWrapper.class);

            if (wrapper.getBody() != null && wrapper.getBody().getItems() != null
                    && wrapper.getBody().getItems().getItem() != null) {
                return wrapper.getBody().getItems().getItem();
            } else {
                return Collections.emptyList();
            }
        }catch (Exception e){
            log.error("XML 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private URI buildUrl(String stationcode, String itemcode){
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("stationcode", stationcode)
                .queryParam("itemcode", itemcode)
                .build(true)
                .toUri();
    }
}
