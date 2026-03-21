package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

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

    @Convert(converter = TripCompanionTypeConverter.class)
    @Column(name = "who_with", length = 20)
    private TripCompanionType whoWithType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "img_url", length = 500)
    private String imgUrl;

    @OneToMany(mappedBy = "tripPlan", fetch = FetchType.LAZY)
    private List<TripPlace> places;

    @Convert(converter = TripRegionConverter.class)
    @Column(name = "region", nullable = false, length = 30)
    private TripRegion regionType;

    @Builder
    private TripPlan(User user,
            String title,
            TripRegion regionType,
            TripCompanionType whoWithType,
            LocalDate startDate,
            LocalDate endDate,
            String imgUrl) {
        this.user = user;
        this.title = title;
        this.regionType = regionType;
        this.whoWithType = whoWithType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.imgUrl = normalizeImageUrl(imgUrl);
    }

    public static TripPlan create(User user,
            String title,
            TripRegion regionType,
            TripCompanionType whoWithType,
            LocalDate startDate,
            LocalDate endDate,
            String imgUrl) {
        return TripPlan.builder()
                .user(user)
                .title(title)
                .regionType(regionType)
                .whoWithType(whoWithType)
                .startDate(startDate)
                .endDate(endDate)
                .imgUrl(imgUrl)
                .build();
    }

    public static TripPlan create(User user,
            String title,
            String regionCode,
            String whoWithCode,
            LocalDate startDate,
            LocalDate endDate,
            String imgUrl) {
        return create(
                user,
                title,
                TripRegion.fromCode(regionCode),
                TripCompanionType.fromCodeOrNull(whoWithCode),
                startDate,
                endDate,
                imgUrl);
    }

    public String getRegion() {
        if (regionType == null) {
            return null;
        }
        return regionType.getCode();
    }

    public String getRegionLabel() {
        if (regionType == null) {
            return "지역 정보 없음";
        }
        return regionType.getLabel();
    }

    public String getWhoWith() {
        if (whoWithType == null) {
            return null;
        }
        return whoWithType.getCode();
    }

    public String getWhoWithLabel() {
        if (whoWithType == null) {
            return "동행 정보 없음";
        }
        return whoWithType.getLabel();
    }

    private static String normalizeImageUrl(String imgUrl) {
        if (imgUrl == null || imgUrl.isBlank()) {
            return null;
        }
        return imgUrl;
    }

    public boolean isOwnedBy(Integer sessionUserId) {
        return sessionUserId != null
                && user != null
                && user.getId() != null
                && user.getId().equals(sessionUserId);
    }
}
