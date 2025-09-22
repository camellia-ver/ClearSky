package com.portfolio.clearSky.api;

import com.portfolio.clearSky.dto.LocationDTO;
import com.portfolio.clearSky.service.AdministrativeBoundaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationApiController {
    private final AdministrativeBoundaryService service;

    /**
     * 자동완성 검색 API
     * @param query 검색어
     * @return 최대 10개의 LocationDTO 리스트
     */
    @GetMapping("/autocomplete")
    public List<LocationDTO> autocomplete(@RequestParam String query) {
        return service.searchAutocomplete(query);
    }
}
