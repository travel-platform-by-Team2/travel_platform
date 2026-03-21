package com.example.travel_platform.booking;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LodgingQueryRepository {

    private final EntityManager em;

    public List<BookingResponse.MapPoiDTO> findActiveLodgingsInBounds(
            String regionKey,
            double minLat,
            double maxLat,
            double minLng,
            double maxLng) {
        return em.createQuery("""
                select new com.example.travel_platform.booking.BookingResponse$MapPoiDTO(
                    l.externalPlaceId,
                    l.name,
                    l.phone,
                    l.address,
                    l.roadAddress,
                    l.placeUrl,
                    l.categoryName,
                    l.categoryGroupCode,
                    l.lat,
                    l.lng,
                    'hotel',
                    'DB'
                )
                from Lodging l
                where l.isActive = true
                  and (:regionKey = '' or l.regionKey = :regionKey)
                  and l.lat between :minLat and :maxLat
                  and l.lng between :minLng and :maxLng
                """, BookingResponse.MapPoiDTO.class)
                .setParameter("regionKey", regionKey)
                .setParameter("minLat", minLat)
                .setParameter("maxLat", maxLat)
                .setParameter("minLng", minLng)
                .setParameter("maxLng", maxLng)
                .getResultList();
    }
}
