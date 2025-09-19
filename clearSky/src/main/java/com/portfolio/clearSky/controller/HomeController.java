package com.portfolio.clearSky.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @Value("${kakao.map.api.key}")
    private String kakaoApiKey;

    @GetMapping("/")
    public String home(Model model){
        model.addAttribute("kakaoKey", kakaoApiKey);
        return "home";
    }
}
