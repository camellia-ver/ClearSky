package com.portfolio.clearSky.repository;

import com.portfolio.clearSky.model.AdministrativeBoundary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
