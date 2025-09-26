package com.portfolio.clearSky.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.portfolio.clearSky.common.cache.MetalCacheKey;
import com.portfolio.clearSky.dto.ItemDto;
import com.portfolio.clearSky.dto.MetalDataResponse;
import com.portfolio.clearSky.dto.ResponseWrapper;
import com.portfolio.clearSky.mapper.MetalDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    private final XmlMapper xmlMapper;
    private final WebClient webClient;
    private final MetalDataMapper metalDataMapper;

    private final AsyncLoadingCache<MetalCacheKey, List<ItemDto>> cache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.HOURS)
            .maximumSize(1000)
            .buildAsync((key, executor) -> fetchDataForKey(key).toFuture());

    public Mono<List<MetalDataResponse>> getMetalDataForLocation(String stationcode, String itemcode) {
        MetalCacheKey key = new MetalCacheKey(
                stationcode,
                itemcode
        );

        return getOrFetch(key)
                .map(metalDataMapper::map);
    }

    private Mono<List<ItemDto>> getOrFetch(MetalCacheKey key) {
        return Mono.defer(() -> {
            CompletableFuture<List<ItemDto>> future = cache.get(key);
            return Mono.fromFuture(future);
        });
    }

    private Mono<List<ItemDto>> fetchDataForKey(MetalCacheKey key){
        String stationcode = key.getStationcode();
        String itemcode = key.getItemcode();

        String url = buildUrl(stationcode, itemcode).toString();

        return fetchDataFromApi(url);
    }

    private Mono<List<ItemDto>> fetchDataFromApi(String url){
        return webClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_XML)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseXml);
    }

    private List<ItemDto> parseXml(String xml){
        try{
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

    /**
     * MetalDataMapper에 정의된 모든 연구소와 항목에 대한 데이터를 비동기적으로 가져옵니다.
     * 모든 조합에 대해 개별 API 호출을 수행하고 그 결과를 하나의 리스트로 결합합니다.
     *
     * @return 모든 MetalDataResponse를 포함하는 Mono<List<MetalDataResponse>>
     */
    public Mono<List<MetalDataResponse>> getAllMetalData(){
        List<String> allStationCodes = List.copyOf(MetalDataMapper.STATION_NAME_MAP.keySet());
        List<String> allItemCodes = List.copyOf(MetalDataMapper.ITEM_NAME_MAP.keySet());

        return Flux.fromIterable(allStationCodes)
                .flatMap(stationCode -> Flux.fromIterable(allItemCodes)
                        .flatMap(itemCode -> getMetalDataForLocation(stationCode, itemCode)
                                .onErrorResume(e -> {
                                    log.error("API 호출 실패 (Station: {}, Item: {}): {}", stationCode, itemCode, e.getMessage());
                                    return Mono.just(Collections.emptyList());
                                })
                        )
                )
                .flatMap(Flux::fromIterable)
                .collectList();
    }
}
