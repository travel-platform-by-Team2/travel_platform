package com.example.travel_platform.booking;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapPlaceImageRepository extends JpaRepository<MapPlaceImage, Long> {
    Optional<MapPlaceImage> findByNormalizedName(String normalizedName);
}
