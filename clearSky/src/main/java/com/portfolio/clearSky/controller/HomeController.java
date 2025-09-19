package com.portfolio.clearSky.controller;

import com.portfolio.clearSky.service.WeatherDataLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final WeatherDataLoader loader;

    @Value("${kakao.map.api.key}")
    private String kakaoApiKey;

    @GetMapping("/")
    public String home(Model model){
        model.addAttribute("kakaoKey", kakaoApiKey);
        loader.fetchUltraShortNowcast();
        return "home";
    }
}
