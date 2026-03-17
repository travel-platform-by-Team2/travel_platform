package com.example.travel_platform.booking;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * 지도 장소 마커(POI)의 이미지 URL을 캐싱하고 관리하는 Repository.
 * 카카오맵 웹 스크래핑 등으로 얻어온 외부 이미지 URL을 DB에 저장(Upsert)하여,
 * 동일한 장소를 조회할 때 스크래핑을 피하고 DB에서 빠르게 이미지를 불러오도록 돕습니다.
 */
@Repository
@RequiredArgsConstructor
public class MapPlaceImageRepository {

    private final JdbcTemplate jdbcTemplate;

    public Optional<String> findImageUrlByNormalizedName(String normalizedName) {
        if (normalizedName == null || normalizedName.isBlank()) {
            return Optional.empty();
        }

        List<String> rows = jdbcTemplate.query(
                "select image_url from map_place_image_tb where normalized_name = ? limit 1",
                (rs, rowNum) -> rs.getString("image_url"),
                normalizedName);

        return rows.stream()
                .filter(url -> url != null && !url.isBlank())
                .findFirst();
    }

    public void upsert(String placeName, String normalizedName, String imageUrl, String source) {
        if (normalizedName == null || normalizedName.isBlank() || imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        int updated = jdbcTemplate.update(
                "update map_place_image_tb set place_name = ?, image_url = ?, source = ?, created_at = current_timestamp where normalized_name = ?",
                placeName == null ? "" : placeName,
                imageUrl,
                source == null ? "UNKNOWN" : source,
                normalizedName);

        if (updated == 0) {
            jdbcTemplate.update(
                    "insert into map_place_image_tb (normalized_name, place_name, image_url, source, created_at) values (?, ?, ?, ?, current_timestamp)",
                    normalizedName,
                    placeName == null ? "" : placeName,
                    imageUrl,
                    source == null ? "UNKNOWN" : source);
        }
    }
}
