package com.portfolio.clearSky.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MapController {
    @GetMapping("/map")
    public String map(){
        return "map";
    }

    @GetMapping("/map-modal")
    public String mapModal(){
        return "map-modal";
    }
}
