package com.portfolio.clearSky.controller;

import com.portfolio.clearSky.dto.ItemDto;
import com.portfolio.clearSky.dto.NearestLocationDto;
import com.portfolio.clearSky.service.AdministrativeBoundaryService;
import com.portfolio.clearSky.service.WeatherService;
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
    public String dashboard(
            @RequestParam(value = "lat") double lat,
            @RequestParam(value = "lng") double lng,
            Model model){

        try {
            NearestLocationDto nearest = administrativeBoundaryService.getNearestGridCoordinates(lat, lng);
            String actualLocationName = nearest.getFullAddress();

            // 1. 사용자에게 알림 메시지를 설정
            if (nearest.getDistanceKm() > 1.0) { // 1km 이상 차이 날 경우 안내
                model.addAttribute("alertMessage",
                        "입력하신 좌표에 가장 가까운 행정 구역은 **" + actualLocationName + "** 입니다. 이 지점의 데이터를 제공합니다."
                );
            }

            model.addAttribute("currentLocation", actualLocationName);

            // 2. 날씨 데이터 호출
            Mono<List<ItemDto>> weatherDataMono = administrativeBoundaryService.getWeatherDataForUserLocation(lat, lng);
            model.addAttribute("weatherData", weatherDataMono);
        }catch (NoSuchElementException e) {
            model.addAttribute("errorMessage", "날씨 정보를 찾을 수 없습니다. 다시 검색해 주세요.");
            return "home";
        }

        return "dashboard";
    }
}
