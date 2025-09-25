package com.portfolio.clearSky.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MetalMeasuringController {

    @GetMapping("/metal-measuring")
    public String map(Model model){
        return "metal-measuring";
    }
}
