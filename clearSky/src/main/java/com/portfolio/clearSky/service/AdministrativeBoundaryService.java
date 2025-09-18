package com.portfolio.clearSky.service;

import com.portfolio.clearSky.model.AdministrativeBoundary;
import com.portfolio.clearSky.repository.AdministrativeBoundaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdministrativeBoundaryService {
    private final AdministrativeBoundaryRepository administrativeBoundaryRepository;

    /**
     * 입력된 level에 따라 적절한 repository 메서드를 호출하여 boundary ID 반환
     */
    public Long getBoundaryId(String level1, String level2, String level3){
        Optional<AdministrativeBoundary> boundaryOpt;

        if (level1 != null && level2 != null && level3 != null) {
            // level1 + level2 + level3
            boundaryOpt = administrativeBoundaryRepository.findByAdmLevel1AndAdmLevel2AndAdmLevel3(level1, level2, level3);
        } else if (level1 != null && level2 != null) {
            // level1 + level2
            boundaryOpt = administrativeBoundaryRepository.findByAdmLevel1AndAdmLevel2(level1, level2);
        } else if (level1 != null && level3 != null) {
            // level1 + level3
            boundaryOpt = administrativeBoundaryRepository.findByAdmLevel1AndAdmLevel3(level1, level3);
        } else if (level2 != null && level3 != null) {
            // level2 + level3
            boundaryOpt = administrativeBoundaryRepository.findByAdmLevel2AndAdmLevel3(level2, level3);
        } else if (level1 != null) {
            // level1 단독
            boundaryOpt = administrativeBoundaryRepository.findByAdmLevel1(level1);
        } else if (level2 != null) {
            // level2 단독
            boundaryOpt = administrativeBoundaryRepository.findByAdmLevel2(level2);
        } else if (level3 != null) {
            // level3 단독
            boundaryOpt = administrativeBoundaryRepository.findByAdmLevel3(level3);
        } else {
            // 모두 null이면 조회 불가
            return null;
        }

        return boundaryOpt.map(AdministrativeBoundary::getId).orElse(null);
    }
}
