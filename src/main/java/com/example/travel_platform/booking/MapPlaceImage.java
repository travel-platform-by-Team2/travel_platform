package com.example.travel_platform.booking;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "map_place_image_tb")
@Getter
@Setter
@NoArgsConstructor
public class MapPlaceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String normalizedName;

    @Column(nullable = false)
    private String placeName;

    @Column(nullable = false, length = 2000)
    private String imageUrl;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
