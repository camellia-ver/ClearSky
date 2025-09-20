package com.portfolio.clearSky.controller;

import com.portfolio.clearSky.dto.ItemDto;
import com.portfolio.clearSky.model.AdministrativeBoundary;
import com.portfolio.clearSky.service.AddressService;
import com.portfolio.clearSky.service.AdministrativeBoundaryLoader;
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
        String fullRegion = addressService.convertRegionName(level1);

        Optional<AdministrativeBoundary> abOpt = administrativeBoundaryService.getBoundary(fullRegion, level2, level3);

        if (abOpt.isEmpty()) {
            log.warn("행정구역을 찾을 수 없습니다: {}, {}, {}", fullRegion, level2, level3);
            model.addAttribute("weatherData", List.of());
            return "dashboard";
        }

        AdministrativeBoundary ab = abOpt.get();

        weatherService.getNowcastForLocation(ab)
                .subscribe(
                        list -> list.forEach(System.out::println),   // 성공 시
                        error -> log.error("Weather API error: {}", error.getMessage()) // 실패 시
                );

        return "dashboard";
    }
}
