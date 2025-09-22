package com.portfolio.clearSky.controller;

import com.portfolio.clearSky.dto.ItemDto;
import com.portfolio.clearSky.model.AdministrativeBoundary;
import com.portfolio.clearSky.service.AddressService;
import com.portfolio.clearSky.service.AdministrativeBoundaryService;
import com.portfolio.clearSky.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final AdministrativeBoundaryService administrativeBoundaryService;
    private final WeatherService weatherService;
    private final AddressService addressService;

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(value="level1", required=false) String level1,
            @RequestParam(value="level2", required=false) String level2,
            @RequestParam(value="level3", required=false) String level3,
            Model model){
//        if (level1 == null || level1.isBlank()) {
//            log.warn("level1 누락");
//            model.addAttribute("weatherData", List.of());
//            return "dashboard";
//        }
//
//        String fullRegion = addressService.convertRegionName(level1);
//        Optional<AdministrativeBoundary> abOpt = administrativeBoundaryService.getBoundary(fullRegion, level2, level3);
//
//        if (abOpt.isEmpty()) {
//            log.warn("행정구역을 찾을 수 없습니다: {}, {}, {}", fullRegion, level2, level3);
//            model.addAttribute("weatherData", List.of());
//            return "dashboard";
//        }
//
//        AdministrativeBoundary ab = abOpt.get();
//        // 비동기 대신 blocking으로 처리
//        List<ItemDto> weatherData = weatherService.getNowcastForLocation(ab)
//                .doOnError(e -> log.error("Weather API error: {}", e.getMessage()))
//                .onErrorReturn(List.of())
//                .block();  // 주의: 요청 지연이 발생할 수 있음
//
//        // 콘솔에 출력
//                System.out.println("weatherData: " + weatherData);
//
//        // 또는 자세히 보기 위해 각 아이템을 반복
//                if (weatherData != null) {
//                    weatherData.forEach(System.out::println);
//                }
//
//        // 모델에 추가
//                model.addAttribute("weatherData", weatherData);


        return "dashboard";
    }
}
