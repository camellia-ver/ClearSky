package com.portfolio.clearSky.controller;

import com.portfolio.clearSky.dto.CombinedWeatherDto;
import com.portfolio.clearSky.dto.NearestLocationDto;
import com.portfolio.clearSky.dto.WeatherDisplayDto;
import com.portfolio.clearSky.service.AdministrativeBoundaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final AdministrativeBoundaryService administrativeBoundaryService;

    @GetMapping("/dashboard")
    public Mono<String> dashboard(
            @RequestParam(value = "lat") double lat,
            @RequestParam(value = "lng") double lng,
            Model model) {
        // 1. 가장 가까운 위치 정보 가져오기
        NearestLocationDto nearestLocation;
        try {
            nearestLocation = administrativeBoundaryService.getNearestGridCoordinates(lat, lng);
        } catch (NoSuchElementException e) {
            // 위치 정보를 찾지 못하면 에러 메시지 설정 후 home으로 리다이렉트 (동기적 처리)
            model.addAttribute("errorMessage", "날씨 정보를 찾을 수 없습니다. 다시 검색해 주세요.");
            return Mono.just("home");
        }

        // 2-1. 초단기 실황 데이터 호출 (Mono)
        Mono<CombinedWeatherDto> nowcastMono =
                administrativeBoundaryService.getNowcastForUserLocation(lat, lng);

        // 2-2. 예보 데이터 호출 (Mono)
        Mono<List<WeatherDisplayDto>> forecastMono =
                administrativeBoundaryService.getForecastForUserLocation(lat, lng);

        // 3. 두 Mono를 Mono.zip을 사용하여 결합
        return Mono.zip(nowcastMono, forecastMono)
                .doOnNext(tuple -> {
                    // Mono에서 데이터가 도착했을 때 Model에 추가
                    CombinedWeatherDto nowcastData = tuple.getT1();
                    List<WeatherDisplayDto> forecastData = tuple.getT2();

                    // 공통 위치 정보 처리
                    String actualLocationName = nearestLocation.getFullAddress();

                    if (nearestLocation.getDistanceKm() > 1.0){
                        model.addAttribute("alertMessage",
                                "입력하신 좌표에 가장 가까운 행정 구역은 **" + actualLocationName + "** 입니다. 이 지점의 데이터를 제공합니다."
                        );
                    }
                    model.addAttribute("currentLocation", actualLocationName);

                    model.addAttribute("weatherData", nowcastData);
                    model.addAttribute("forecastDataList", forecastData);
                })
                .map(tuple -> "dashboard")
                .onErrorResume(e -> {
                    log.error("날씨 데이터를 가져오는 중 심각한 오류 발생", e);
                    model.addAttribute("errorMessage", "날씨 데이터를 가져오는 데 실패했습니다. 잠시 후 다시 시도해 주세요.");
                    return Mono.just("home");
                });
    }
}
