package com.portfolio.clearSky.service;

import com.portfolio.clearSky.model.AdministrativeBoundary;
import com.portfolio.clearSky.repository.AdministrativeBoundaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdministrativeBoundaryService {
    private final AdministrativeBoundaryRepository repository;

    /**
     * level1~level3를 기반으로 AdministrativeBoundary 조회
     * @param level1 1단계 행정구역 (필수)
     * @param level2 2단계 행정구역 (선택)
     * @param level3 3단계 행정구역 (선택)
     * @return 조회 결과 Optional
     */
    public Optional<AdministrativeBoundary> getBoundary(String level1, String level2, String level3){
        if (level1 == null || level1.isBlank()) {
            // level1 없으면 조회 불가
            return Optional.empty();
        }

        Optional<AdministrativeBoundary> boundaryOpt;

        if (level2 != null && !level2.isBlank() && level3 != null && !level3.isBlank()) {
            // level1 정확히 + level2 일부 + level3 일부
            boundaryOpt = repository.findByAdmLevel1AndAdmLevel2ContainingAndAdmLevel3Containing(level1, level2, level3);
        } else if (level2 != null && !level2.isBlank()) {
            // level1 정확히 + level2 일부 (level3 null)
            boundaryOpt = repository.findByAdmLevel1AndAdmLevel2ContainingAndAdmLevel3IsNull(level1, level2);
        } else {
            // level1 정확히만
            boundaryOpt = repository.findByAdmLevel1AndAdmLevel2IsNullAndAdmLevel3IsNull(level1);
        }

        return boundaryOpt;
    }
}
