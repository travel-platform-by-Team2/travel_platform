package com.example.travel_platform.booking;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LodgingQueryRepository {

    private final EntityManager em;

    /**
     * JPA 전용 쿼리(JPQL)를 사용하여 숙소 목록을 조회합니다.
     * SQL과 비슷하지만, 테이블이 아닌 '객체(Lodging)'를 대상으로 쿼리합니다.
     */
    public List<Lodging> findActiveLodgingsInBounds(
            String regionKey,
            BigDecimal minLat, BigDecimal maxLat,
            BigDecimal minLng, BigDecimal maxLng) {

        String jpql = "SELECT l FROM Lodging l " +
                      "WHERE l.isActive = true " +
                      "AND (:regionKey = '' OR l.regionKey = :regionKey) " +
                      "AND l.lat BETWEEN :minLat AND :maxLat " +
                      "AND l.lng BETWEEN :minLng AND :maxLng";

        TypedQuery<Lodging> query = em.createQuery(jpql, Lodging.class);
        query.setParameter("regionKey", regionKey);
        query.setParameter("minLat", minLat);
        query.setParameter("maxLat", maxLat);
        query.setParameter("minLng", minLng);
        query.setParameter("maxLng", maxLng);

        // 결과는 JPA가 자동으로 Lodging 객체 리스트로 조립해줍니다.
        return query.getResultList();
    }
}
