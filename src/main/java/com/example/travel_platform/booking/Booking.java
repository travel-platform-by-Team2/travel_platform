package com.example.travel_platform.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.travel_platform.trip.TripPlan;
import com.example.travel_platform.user.User;

import jakarta.persistence.Column;
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

    @Column(name = "location", nullable = false, length = 50)
    private String location;

    @Column(name = "image_url")
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @lombok.Builder
    private Booking(User user, TripPlan tripPlan, String lodgingName, LocalDate checkIn, LocalDate checkOut,
            Integer guestCount, Integer pricePerNight, Integer taxAndServiceFee, String location, String imageUrl) {
        this.user = user;
        this.tripPlan = tripPlan;
        this.lodgingName = lodgingName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guestCount = guestCount;
        this.pricePerNight = pricePerNight;
        this.taxAndServiceFee = taxAndServiceFee;
        this.location = location;
        this.imageUrl = imageUrl;
    }

    public static Booking create(User user, TripPlan tripPlan, String lodgingName, LocalDate checkIn, LocalDate checkOut,
            Integer guestCount, Integer pricePerNight, Integer taxAndServiceFee, String location, String imageUrl) {
        return Booking.builder()
                .user(user)
                .tripPlan(tripPlan)
                .lodgingName(lodgingName)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .guestCount(guestCount)
                .pricePerNight(pricePerNight)
                .taxAndServiceFee(taxAndServiceFee)
                .location(location)
                .imageUrl(imageUrl)
                .build();
    }
}
