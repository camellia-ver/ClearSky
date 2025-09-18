package com.portfolio.clearSky.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DashboardController {
    @GetMapping("/dashboard")
    public String dashboard(){
        return "dashboard";
    }
}
