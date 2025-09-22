package com.portfolio.clearSky.repository;

import com.portfolio.clearSky.dto.NearestLocationDto;
import com.portfolio.clearSky.model.AdministrativeBoundary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdministrativeBoundaryRepository extends JpaRepository<AdministrativeBoundary, Long> {
    /**
     * 1/2/3단계 주소를 결합하여 검색
     * JPA는 Pageable 객체를 사용하여 쿼리 결과 개수를 DB 레벨에서 제한 가능
     */
    @Query(value =
            "SELECT ab FROM AdministrativeBoundary ab " +
                    "WHERE CONCAT(ab.admLevel1, ' ', ab.admLevel1, ' ', ab.admLevel1) LIKE %:query%"
    )
    List<AdministrativeBoundary> findByCombinedAddressContaining(
            @Param("query") String query,
            Pageable pageable
    );

    /**
     * 사용자가 입력한 위/경도와 가장 가까운 DB에 저장된 위치의 gridx, gridy를 찾는 Native Query.
     *
     * @param targetLat 사용자가 검색한 위도
     * @param targetLng 사용자가 검색한 경도
     * @return 가장 가까운 위치 정보 (NearestLocationDto)
     */
    @Query(value = """
                SELECT
                    ab.gridx, 
                    ab.gridy,
                    (
                        6371 * ACOS(
                            COS(RADIANS(:targetLat)) * COS(RADIANS(ab.lat_deg + (ab.lat_min / 60) + (ab.lat_sec + (ab.lat_sec100 / 100)) / 3600)) * COS(RADIANS(ab.lon_deg + (ab.lon_min / 60) + (ab.lon_sec + (ab.lon_sec100 / 100)) / 3600) - RADIANS(:targetLng)) + 
                            SIN(RADIANS(:targetLat)) * SIN(RADIANS(ab.lat_deg + (ab.lat_min / 60) + (ab.lat_sec + (ab.lat_sec100 / 100)) / 3600))
                        )
                    ) AS distanceKm,
                      ab.adm_level1,
                      ab.adm_level2,
                      ab.adm_level3
                FROM 
                    administrative_boundary ab
                ORDER BY 
                    distanceKm
                LIMIT 1
            """, nativeQuery = true)
    Optional<NearestLocationDto> findNearestLocationDto(@Param("targetLat") double targetLat, @Param("targetLng") double targetLng);
}