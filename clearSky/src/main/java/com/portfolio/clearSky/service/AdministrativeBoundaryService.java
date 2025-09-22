package com.portfolio.clearSky.service;

import com.portfolio.clearSky.dto.LocationDTO;
import com.portfolio.clearSky.dto.NearestLocationDto;
import com.portfolio.clearSky.model.AdministrativeBoundary;
import com.portfolio.clearSky.repository.AdministrativeBoundaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdministrativeBoundaryService {
    private final AdministrativeBoundaryRepository repository;

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

    public NearestLocationDto getOptimalLocation(double lat, double lng) {
        return repository.findNearestLocationDto(lat, lng)
                .orElseThrow(() -> new NoSuchElementException("가장 가까운 위치를 DB에서 찾을 수 없습니다. (DB가 비어있을 가능성)"));
    }
}
