package com.example.travel_platform.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.trip.TripRegion;
import com.example.travel_platform.trip.TripRegionConverter;
import com.example.travel_platform.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "booking_tb")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id", nullable = false)
    private TripPlan tripPlan;

    @Column(name = "lodging_name", nullable = false, length = 120)
    private String lodgingName;

    @Column(name = "room_name", nullable = false, length = 120)
    private String roomName;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;

    @Column(name = "price_per_night", nullable = false)
    private Integer pricePerNight;

    @Column(name = "tax_and_service_fee", nullable = false)
    private Integer taxAndServiceFee;

    @Convert(converter = TripRegionConverter.class)
    @Column(name = "region_key", nullable = false, length = 30)
    private TripRegion regionType;

    @Column(name = "image_url")
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @lombok.Builder
    private Booking(
            User user,
            TripPlan tripPlan,
            String lodgingName,
            String roomName,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guestCount,
            Integer pricePerNight,
            Integer taxAndServiceFee,
            TripRegion regionType,
            String imageUrl) {
        this.user = user;
        this.tripPlan = tripPlan;
        this.lodgingName = lodgingName;
        this.roomName = normalizeRoomName(roomName);
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guestCount = guestCount;
        this.pricePerNight = pricePerNight;
        this.taxAndServiceFee = taxAndServiceFee;
        this.regionType = regionType;
        this.imageUrl = normalizeImageUrl(imageUrl);
    }

    public static Booking create(
            User user,
            TripPlan tripPlan,
            String lodgingName,
            String roomName,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guestCount,
            Integer pricePerNight,
            Integer taxAndServiceFee,
            TripRegion regionType,
            String imageUrl) {
        return Booking.builder()
                .user(user)
                .tripPlan(tripPlan)
                .lodgingName(lodgingName)
                .roomName(roomName)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .guestCount(guestCount)
                .pricePerNight(pricePerNight)
                .taxAndServiceFee(taxAndServiceFee)
                .regionType(regionType)
                .imageUrl(imageUrl)
                .build();
    }

    public static Booking create(
            User user,
            TripPlan tripPlan,
            String lodgingName,
            String roomName,
            LocalDate checkIn,
            LocalDate checkOut,
            Integer guestCount,
            Integer pricePerNight,
            Integer taxAndServiceFee,
            String regionKey,
            String imageUrl) {
        return create(
                user,
                tripPlan,
                lodgingName,
                roomName,
                checkIn,
                checkOut,
                guestCount,
                pricePerNight,
                taxAndServiceFee,
                TripRegion.fromCode(regionKey),
                imageUrl);
    }

    public String getRegionKey() {
        if (regionType == null) {
            return BookVar.DEFAULT_REGION_KEY;
        }
        return regionType.getCode();
    }

    public String getLocation() {
        if (regionType == null) {
            return BookVar.DEFAULT_LOCATION_NAME;
        }
        return regionType.getLabel();
    }

    private static String normalizeRoomName(String roomName) {
        if (roomName == null || roomName.isBlank()) {
            return BookVar.DEFAULT_ROOM_NAME;
        }
        return roomName;
    }

    private static String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        return imageUrl;
    }
}
