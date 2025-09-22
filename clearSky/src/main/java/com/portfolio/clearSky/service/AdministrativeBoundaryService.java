package com.portfolio.clearSky.service;

import com.portfolio.clearSky.dto.ItemDto;
import com.portfolio.clearSky.dto.LocationDTO;
import com.portfolio.clearSky.dto.NearestLocationDto;
import com.portfolio.clearSky.model.AdministrativeBoundary;
import com.portfolio.clearSky.repository.AdministrativeBoundaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdministrativeBoundaryService {
    private final AdministrativeBoundaryRepository repository;
    private final WeatherService weatherService;

    /**
     * DB를 조회하여 자동완성 결과를 반환합니다.
     * @param query 사용자가 입력한 검색어
     * @return 검색된 LocationDto 리스트 (최대 10개)
     */
    public List<LocationDTO> searchAutocomplete(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }

        // Pageable 객체를 사용하여 DB 쿼리 레벨에서 결과 개수를 10개로 제한
        Pageable topTen = PageRequest.of(0, 10);

        // 1. Repository를 사용하여 DB에서 주소 검색
        List<AdministrativeBoundary> entities = repository.findByCombinedAddressContaining(query, topTen);

        // 2. 검색 결과가 많을 경우를 대비해 상위 10개만 DTO로 변환하여 클라이언트에 전달 (최적화)
        return entities.stream()
                .map(this::convertToDto) // Entity를 DTO로 변환
                .collect(Collectors.toList());
    }

    // Entity를 DTO로 변환하는 헬퍼 메서드
    private LocationDTO convertToDto(AdministrativeBoundary entity) {
        LocationDTO dto = new LocationDTO();
        dto.setFull_address(entity.getFullAddress());

        // 위도(latitude)를 십진수로 변환하여 설정
        if (entity.getLatitude() != null) {
            dto.setLat(entity.getLatitude().toDecimal());
        }

        // 경도(longitude)를 십진수로 변환하여 설정
        if (entity.getLongitude() != null) {
            dto.setLng(entity.getLongitude().toDecimal());
        }

        return dto;
    }

    /**
     * 사용자 좌표(lat, lng)를 기준으로 DB에서 가장 가까운 위치의 Grid 좌표를 찾음
     * 이 메서드는 다른 서비스/컨트롤러에서 유효한 gridX, gridY를 얻기 위해 사용
     */
    public NearestLocationDto getNearestGridCoordinates(double lat, double lng) {
        return repository.findNearestLocationDto(lat, lng)
                .orElseThrow(() -> new NoSuchElementException("가장 가까운 위치를 DB에서 찾을 수 없습니다. (DB가 비어있을 가능성)"));
    }

    /**
     * 사용자 위치에 대한 초단기 실황 및 예보 데이터를 모두 가져오는 통합 함수
     */
    public Mono<List<ItemDto>> getWeatherDataForUserLocation(double lat, double lng){
        // 1. 사용자 좌표를 DB의 가장 가까운 유효한 gridX, gridY로 변환
        NearestLocationDto nearestLocation;

        try {
            nearestLocation = getNearestGridCoordinates(lat, lng);
        } catch (NoSuchElementException e) {
            // 위치 정보를 찾지 못하면 빈 Mono를 반환하여 호출자(Controller)가 오류를 처리
            return Mono.error(e);
        }

        Integer gridX = nearestLocation.getGridX();
        Integer gridY = nearestLocation.getGridY();

        // 2. WeatherService를 사용하여 두 종류의 기상 데이터를 비동기적 반환받음
        Mono<List<ItemDto>> nowcastMono = weatherService.getNowcastForLocation(gridX, gridY);
        Mono<List<ItemDto>> forecastMono = weatherService.getForecastForLocation(gridX, gridY);

        // 3. 두 Mono를 합쳐 하나의 Mono<List<ItemDto>>로 만듬
        //    Mono.zip을 사용하여 두 API 호출이 모두 완료될 때까지 기다린 후 결과를 병합
        return Mono.zip(nowcastMono, forecastMono)
                .map(tuple -> {
                    List<ItemDto> combinedList = new ArrayList<>();
                    combinedList.addAll(tuple.getT1()); // 초단기 실황 결과
                    combinedList.addAll(tuple.getT2()); // 초단기 예보 결과
                    return combinedList;
                })
                // 만약 하나라도 오류가 발생하면, 오류를 전파
                .onErrorResume(e -> {
                    return Mono.error(new RuntimeException("날씨 데이터를 가져오는 중 오류가 발생했습니다.", e));
                });
    }
}
