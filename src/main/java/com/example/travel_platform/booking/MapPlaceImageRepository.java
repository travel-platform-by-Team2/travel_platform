package com.example.travel_platform.booking;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MapPlaceImageRepository extends JpaRepository<MapPlaceImage, Integer>, MapPlaceImageRepositoryCustom {

    @Query("""
            select mi.imageUrl
            from MapPlaceImage mi
            where mi.normalizedName = :normalizedName
            """)
    Optional<String> findImageUrlByNormalizedName(@Param("normalizedName") String normalizedName);
}
