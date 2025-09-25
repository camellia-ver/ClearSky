package com.portfolio.clearSky.controller;

import com.portfolio.clearSky.dto.MetalDataResponse;
import com.portfolio.clearSky.service.MetalMeasuringService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MapController {
    private final MetalMeasuringService service;

    @Value("${kakao.map.api.key}")
    private String kakaoApiKey;

    @GetMapping("/map")
    public String map(Model model){
        model.addAttribute("kakaoKey", kakaoApiKey);
        return "map";
    }
}
