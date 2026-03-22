package com.example.travel_platform.booking.lodging;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LodgingQueryRepository {

    private final EntityManager em;

    public List<LodgingPoiRow> findActiveLodgingsInBounds(
            String regionKey,
            double minLat,
            double maxLat,
            double minLng,
            double maxLng) {
        List<Tuple> tuples = em.createQuery("""
                select
                    l.externalPlaceId as externalPlaceId,
                    l.name as name,
                    l.phone as phone,
                    l.address as address,
                    l.roadAddress as roadAddress,
                    l.placeUrl as placeUrl,
                    l.categoryName as categoryName,
                    l.categoryGroupCode as categoryGroupCode,
                    l.lat as lat,
                    l.lng as lng
                from Lodging l
                where l.isActive = true
                  and (:regionKey = '' or l.regionKey = :regionKey)
                  and l.lat between :minLat and :maxLat
                  and l.lng between :minLng and :maxLng
                """, Tuple.class)
                .setParameter("regionKey", regionKey)
                .setParameter("minLat", minLat)
                .setParameter("maxLat", maxLat)
                .setParameter("minLng", minLng)
                .setParameter("maxLng", maxLng)
                .getResultList();

        List<LodgingPoiRow> rows = new ArrayList<>();
        for (Tuple tuple : tuples) {
            rows.add(toLodgingPoiRow(tuple));
        }
        return rows;
    }

    private LodgingPoiRow toLodgingPoiRow(Tuple tuple) {
        return new LodgingPoiRow(
                tuple.get("externalPlaceId", String.class),
                tuple.get("name", String.class),
                tuple.get("phone", String.class),
                tuple.get("address", String.class),
                tuple.get("roadAddress", String.class),
                tuple.get("placeUrl", String.class),
                tuple.get("categoryName", String.class),
                tuple.get("categoryGroupCode", String.class),
                tuple.get("lat", Double.class),
                tuple.get("lng", Double.class));
    }
}
