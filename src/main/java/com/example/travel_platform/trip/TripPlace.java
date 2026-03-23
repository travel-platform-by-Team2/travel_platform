package com.example.travel_platform.trip;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 여행 계획에 포함되는 개별 장소를 표현하는 엔티티.
 * 장소명, 주소, 좌표, 일차 순서 등 상세 항목을 담당한다.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "trip_place_tb")
public class TripPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id", nullable = false)
    private TripPlan tripPlan;

    @Column(name = "place_name", nullable = false, length = 100)
    private String placeName;

    @Column(length = 255)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "trip_day", nullable = false)
    private Integer tripDay;

    @Column(length = 511)
    private String imgUrl;

    @Builder
    private TripPlace(TripPlan tripPlan,
            String placeName,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            Integer tripDay,
            String imgUrl) {
        this.tripPlan = tripPlan;
        this.placeName = placeName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tripDay = tripDay;
        this.imgUrl = imgUrl;
    }

    public static TripPlace create(TripPlan tripPlan, String placeName, String address, BigDecimal latitude,
            BigDecimal longitude, Integer tripDay) {
        return create(tripPlan, placeName, address, latitude, longitude, tripDay, null);
    }

    public static TripPlace create(TripPlan tripPlan, String placeName, String address, BigDecimal latitude,
            BigDecimal longitude, Integer tripDay, String imgUrl) {
        return TripPlace.builder()
                .tripPlan(tripPlan)
                .placeName(placeName)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .tripDay(tripDay)
                .imgUrl(imgUrl)
                .build();
    }

    public Integer getDayOrder() {
        return tripDay;
    }
}
