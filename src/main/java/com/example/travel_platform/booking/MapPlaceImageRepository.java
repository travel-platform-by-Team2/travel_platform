package com.example.travel_platform.booking;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MapPlaceImageRepository {

    private final EntityManager em;

    public Optional<MapPlaceImage> findByNormalizedName(String normalizedName) {
        if (normalizedName == null || normalizedName.isBlank()) {
            return Optional.empty();
        }

        try {
            // 단건 조회 시 NoResultException 처리가 필요합니다.
            MapPlaceImage image = em.createQuery(
                    "SELECT i FROM MapPlaceImage i WHERE i.normalizedName = :name", MapPlaceImage.class)
                    .setParameter("name", normalizedName)
                    .getSingleResult();
            return Optional.of(image);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Upsert (Save or Update) 구현.
     * JPA는 영속성 컨텍스트(Persistence Context)를 통해 객체의 상태를 관리하므로
     * 조각난 SQL을 일일이 날릴 필요 없이 객체 중심으로 처리합니다.
     */
    public void save(MapPlaceImage image) {
        if (image.getId() == null) {
            // ID가 없으면 새로운 데이터이므로 저장(Insert)
            em.persist(image);
        } else {
            // ID가 있으면 기존 데이터이므로 병합(Update)
            em.merge(image);
        }
    }
}
