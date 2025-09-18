package com.portfolio.clearSky.controller;

import com.portfolio.clearSky.service.AdministrativeBoundaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final AdministrativeBoundaryService administrativeBoundaryService;

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(value="level1", required=false) String level1,
            @RequestParam(value="level2", required=false) String level2,
            @RequestParam(value="level3", required=false) String level3,
            Model model){


        return "dashboard";
    }
}
