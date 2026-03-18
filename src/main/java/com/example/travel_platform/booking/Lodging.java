package com.example.travel_platform.booking;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 숙소(Lodging) 정보를 저장하는 엔티티.
 * 지도 화면의 POI 마커 조회를 위해 사용됩니다.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "lodging_tb")
public class Lodging {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_place_id", unique = true, nullable = false, length = 64)
    private String externalPlaceId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 200)
    private String normalizedName;

    @Column(name = "category_group_code", nullable = false, length = 10)
    private String categoryGroupCode;

    @Column(name = "category_name", length = 300)
    private String categoryName;

    @Column(length = 50)
    private String phone;

    @Column(length = 300)
    private String address;

    @Column(name = "road_address", length = 300)
    private String roadAddress;

    @Column(name = "region_key", nullable = false, length = 50)
    private String regionKey;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(name = "place_url", length = 500)
    private String placeUrl;

    @Column(name = "room_price", nullable = false)
    private Integer roomPrice = 0;

    @Column(nullable = false)
    private Integer fee = 0;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
