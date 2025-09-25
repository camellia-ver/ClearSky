package com.portfolio.clearSky.api;

import com.portfolio.clearSky.dto.MetalDataResponse;
import com.portfolio.clearSky.service.MetalMeasuringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/metal")
@RequiredArgsConstructor
public class MetalMeasuringApiController {
    private final MetalMeasuringService service;

    /**
     * 전체 연구소/항목 조합에 대한 금속 데이터를 비동기적으로 가져옵니다.
     * 엔드포인트: GET /api/metal/all
     *
     * @return Mono<List<MetalDataResponse>> 모든 금속 데이터를 담은 리액티브 스트림
     */
    @GetMapping("/all")
    public Mono<List<MetalDataResponse>> getAllMetalData(){
        return service.getAllMetalData();
    }
}
