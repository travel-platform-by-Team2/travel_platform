package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.example.travel_platform.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "trip_plan_tb")
public class TripPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "who_with")
    private String whoWith;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "img_url", nullable = false, length = 500)
    private String imgUrl;

    @OneToMany(mappedBy = "tripPlan", fetch = FetchType.LAZY)
    private List<TripPlace> places;

    @Column(name = "region", nullable = false, length = 30)
    private String region;

    @Builder
    private TripPlan(User user,
            String title,
            String region,
            String whoWith,
            LocalDate startDate,
            LocalDate endDate,
            String imgUrl) {
        this.user = user;
        this.title = title;
        this.region = region;
        this.whoWith = whoWith;
        this.startDate = startDate;
        this.endDate = endDate;
        this.imgUrl = imgUrl;
    }

    public static TripPlan create(User user,
            String title,
            String region,
            String whoWith,
            LocalDate startDate,
            LocalDate endDate,
            String imgUrl) {
        return TripPlan.builder()
                .user(user)
                .title(title)
                .region(region)
                .whoWith(whoWith)
                .startDate(startDate)
                .endDate(endDate)
                .imgUrl(imgUrl)
                .build();
    }
}
