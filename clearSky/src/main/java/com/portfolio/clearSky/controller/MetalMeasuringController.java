package com.portfolio.clearSky.controller;

import com.portfolio.clearSky.dto.MetalDataResponse;
import com.portfolio.clearSky.dto.StationMetalDataDto;
import com.portfolio.clearSky.service.MetalMeasuringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MetalMeasuringController {
    private final MetalMeasuringService service;

    @GetMapping("/metal-measuring")
    public Mono<String> map(Model model){
        return service.getAllMetalData()
                .map(responseList -> {
                    Map<String, List<MetalDataResponse>> groupedByStation = responseList.stream()
                            .collect(Collectors.groupingBy(MetalDataResponse::getStationName));
                    return groupedByStation.entrySet().stream()
                            .map(entry -> {
                                String stationName = entry.getKey();
                                List<MetalDataResponse> stationResponses = entry.getValue();
                                Map<String, String> itemDataMap = stationResponses.stream()
                                        .collect(Collectors.toMap(
                                                MetalDataResponse::getItemName, // 키: "납", "니켈" 등
                                                MetalDataResponse::getItemValue, // 값: "1.234", "0.5" 등
                                                (existing, replacement) -> existing // 병합 시 충돌 방지 (여기서는 최신 값 유지)
                                        ));
                                // 최종 DTO 생성
                                return new StationMetalDataDto(stationName, itemDataMap);
                            })
                            .collect(Collectors.toList());
                })
                .doOnNext(stationDataList -> {
                    model.addAttribute("stationDataList", stationDataList);
                    log.info("변환된 금속 데이터 {} 건을 모델에 성공적으로 추가했습니다.", stationDataList.size());
                })
                .map(stationDataList -> "metal-measuring")
                .onErrorResume(e -> {
                    log.error("금속 데이터를 가져오거나 변환하는 중 오류 발생", e);
                    model.addAttribute("errorMessage", "데이터 처리 중 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.");
                    return Mono.just("home");
                });
    }
}
