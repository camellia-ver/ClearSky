package com.portfolio.clearSky.service;

import com.portfolio.clearSky.dto.*;
import com.portfolio.clearSky.mapper.WeatherCategoryMapper;
import com.portfolio.clearSky.model.AdministrativeBoundary;
import com.portfolio.clearSky.repository.AdministrativeBoundaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdministrativeBoundaryService {
    private static final DateTimeFormatter DATE_INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_INPUT_FORMATTER = DateTimeFormatter.ofPattern("HHmm");
    private static final DateTimeFormatter DATE_OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 M월 d일");
    private static final DateTimeFormatter TIME_OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");


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

    public Mono<CombinedWeatherDto> getNowcastForUserLocation(double lat, double lng){
        NearestLocationDto nearestLocation;

        try {
            nearestLocation = getNearestGridCoordinates(lat, lng);
        } catch (NoSuchElementException e) {
            // 위치 정보를 찾지 못하면 오류 Mono를 반환
            return Mono.error(e);
        }

        Integer gridX = nearestLocation.getGridX();
        Integer gridY = nearestLocation.getGridY();

        Mono<List<ItemDto>> nowcastMono = weatherService.getNowcastForLocation(gridX, gridY);

        return nowcastMono
                .map(this::convertToCombinedDtoFromItem)
                .onErrorResume(e -> {
                    return Mono.error(new RuntimeException("초단기 실황 데이터를 가져오는 중 오류가 발생했습니다.", e));
                });
    }

    private CombinedWeatherDto convertToCombinedDtoFromItem(List<ItemDto> itemDtos){
        CombinedWeatherDto combinedDto = new CombinedWeatherDto();

        double uuu = 0.0;
        double vvv = 0.0;

        for (ItemDto dto : itemDtos) {
            String category = dto.getCategory();
            String value = dto.getObsrValue();

            switch (category) {
                case "T1H":
                    // 1시간 기온
                    combinedDto.setTemperature(Double.parseDouble(value));
                    break;
                case "REH":
                    // 습도
                    combinedDto.setHumidity(Integer.parseInt(value));
                    break;
                case "PTY":
                    int ptyCode = Integer.parseInt(value);
                    String ptyString = combinedDto.getPrecipitationTypeString(ptyCode);
                    combinedDto.setPrecipitationType(ptyString);
                    break;
                case "RN1":
                    // 1시간 강수량
                    combinedDto.setPrecipitationAmount(Double.parseDouble(value));
                    break;
                case "WSD":
                    // 풍속
                    combinedDto.setWindSpeed(Double.parseDouble(value));
                    break;
                case "UUU":
                    // 동서바람성분
                    uuu = Double.parseDouble(value);
                    break;
                case "VVV":
                    // 남북바람성분
                    vvv = Double.parseDouble(value);
                    break;
                case "VEC":
                    // 풍향 (deg)
                    combinedDto.setWindDirectionDegrees(Double.parseDouble(value));
                    break;
            }
        }

        final double THRESHOLD = 0.1;
        String windDirection = "";

        if (Math.abs(uuu) < THRESHOLD && Math.abs(vvv) < THRESHOLD){
            // 바람이 거의 없을 때
            windDirection = "무풍";
        }else {
            // 1. 남북 성분 (VVV)을 먼저 분석하여 방향 문자열의 앞부분 결정 (남/북)
            if (vvv < -THRESHOLD) {
                windDirection += "북"; // VVV가 음수(남쪽 성분) -> 북쪽에서 옴
            } else if (vvv > THRESHOLD) {
                windDirection += "남"; // VVV가 양수(북쪽 성분) -> 남쪽에서 옴
            }

            // 2. 동서 성분 (UUU)을 분석하여 방향 문자열의 뒷부분 결정 (동/서)
            if (uuu < -THRESHOLD) {
                windDirection += "동"; // UUU가 음수(서쪽 성분) -> 동쪽에서 옴
            } else if (uuu > THRESHOLD) {
                windDirection += "서"; // UUU가 양수(동쪽 성분) -> 서쪽에서 옴
            }

            // 최종적으로 '풍'을 붙여줍니다.
            if (!windDirection.isEmpty() && !windDirection.equals("무풍")) {
                windDirection += "풍";
            }
        }

        // 풍속 (합성 속도) 계산
        // 피타고라스 정리: Speed = sqrt(UUU^2 + VVV^2)
        double windSpeed = Math.sqrt(uuu * uuu + vvv * vvv);

        combinedDto.setCalculatedWindDirectionString(windDirection);
        combinedDto.setWindSpeed(windSpeed);

        return combinedDto;
    }

    public Mono<List<WeatherDisplayDto>> getForecastForUserLocation(double lat, double lng){
        NearestLocationDto nearestLocation;

        try {
            nearestLocation = getNearestGridCoordinates(lat, lng);
        } catch (NoSuchElementException e) {
            // 위치 정보를 찾지 못하면 오류 Mono를 반환
            return Mono.error(e);
        }

        Integer gridX = nearestLocation.getGridX();
        Integer gridY = nearestLocation.getGridY();

        Mono<List<ItemDto>> forecastMono = weatherService.getForecastForLocation(gridX, gridY);

        return forecastMono
                .map(itemDtos -> itemDtos.stream()
                        .map(this::convertToDisplayDto)
                        .collect(Collectors.toList()))
                .onErrorResume(e -> {
                    return Mono.error(new RuntimeException("초단기 실황 데이터를 가져오는 중 오류가 발생했습니다.", e));
                });
    }

    private WeatherDisplayDto convertToDisplayDto(ItemDto itemDto) {
        WeatherDisplayDto displayDto = new WeatherDisplayDto();

        String categoryName = WeatherCategoryMapper.getCategoryName(itemDto.getCategory());
        displayDto.setCategoryName(categoryName);
        displayDto.setCategory(itemDto.getCategory());

        if (itemDto.getBaseDate() != null) {
            LocalDate baseDate = LocalDate.parse(itemDto.getBaseDate(), DATE_INPUT_FORMATTER);
            displayDto.setBaseDate(baseDate.format(DATE_OUTPUT_FORMATTER));
        }

        if (itemDto.getBaseTime() != null) {
            LocalTime baseTime = LocalTime.parse(itemDto.getBaseTime(), TIME_INPUT_FORMATTER);
            displayDto.setBaseTime(baseTime.format(TIME_OUTPUT_FORMATTER));
        }

        if (itemDto.getFcstDate() != null) {
            LocalDate fcstDate = LocalDate.parse(itemDto.getFcstDate(), DATE_INPUT_FORMATTER);
            displayDto.setFcstDate(fcstDate.format(DATE_OUTPUT_FORMATTER));
        }

        if (itemDto.getFcstTime() != null) {
            LocalTime fcstTime = LocalTime.parse(itemDto.getFcstTime(), TIME_INPUT_FORMATTER);
            displayDto.setFcstTime(fcstTime.format(TIME_OUTPUT_FORMATTER));
        }

        String unit = WeatherCategoryMapper.getUnit(itemDto.getCategory());

        if (itemDto.getObsrValue() != null && !itemDto.getObsrValue().isEmpty()) {
            String obsrValueWithUnit = itemDto.getObsrValue() + unit;
            displayDto.setObsrValue(obsrValueWithUnit);
        } else {
            displayDto.setObsrValue("-");
        }

        if (itemDto.getFcstValue() != null && !itemDto.getFcstValue().isEmpty()) {
            String fcstValueWithUnit = itemDto.getFcstValue() + unit;
            displayDto.setFcstValue(fcstValueWithUnit);
        } else {
            displayDto.setFcstValue("-");
        }

        displayDto.setNx(itemDto.getNx());
        displayDto.setNy(itemDto.getNy());

        return displayDto;
    }
}
