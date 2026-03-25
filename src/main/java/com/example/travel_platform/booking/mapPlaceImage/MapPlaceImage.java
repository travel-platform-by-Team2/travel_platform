package com.example.travel_platform.booking.mapPlaceImage;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 지도 장소 이미지 캐시 정보를 저장하는 엔티티.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "map_place_image_tb")
public class MapPlaceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "normalized_name", unique = true, nullable = false, length = 200)
    private String normalizedName;

    @Column(name = "place_name", nullable = false, length = 200)
    private String placeName;

    @Column(name = "image_url", nullable = false, length = 2000)
    private String imageUrl;

    @Column(nullable = false, length = 50)
    private String source;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
