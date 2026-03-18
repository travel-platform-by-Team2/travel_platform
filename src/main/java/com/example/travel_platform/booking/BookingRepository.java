package com.example.travel_platform.booking;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 예약(Booking) 엔티티에 대한 기본 CRUD와 더불어,
 * 숙소(Lodging) 조회 및 장소 이미지 캐싱 기능을 통합 관리하는 리포지토리입니다.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    /**
     * 특정 지도 경계 영역 내의 활성화된 숙소 목록을 조회하여 DTO로 즉시 반환합니다.
     */
    @Query("""
            SELECT new com.example.travel_platform.booking.BookingRequest$MapPoiDTO(
                l.externalPlaceId, 
                l.name, 
                l.phone, 
                l.address, 
                l.roadAddress, 
                l.placeUrl, 
                l.categoryName, 
                l.categoryGroupCode, 
                l.lat, 
                l.lng, 
                'hotel', 
                'DB'
            )
            FROM Lodging l
            WHERE l.isActive = true
            AND (:regionKey = '' OR l.regionKey = :regionKey)
            AND l.lat BETWEEN :minLat AND :maxLat
            AND l.lng BETWEEN :minLng AND :maxLng
            """)
    List<BookingRequest.MapPoiDTO> findActiveLodgingsInBounds(
            @Param("regionKey") String regionKey, // 지역 키 (예: 'seoul', 'busan', 'jeju' 등)
            @Param("minLat") double minLat,       // 지도 화면 내 최소 위도 (남쪽 경계)
            @Param("maxLat") double maxLat,       // 지도 화면 내 최대 위도 (북쪽 경계)
            @Param("minLng") double minLng,       // 지도 화면 내 최소 경도 (서쪽 경계)
            @Param("maxLng") double maxLng);      // 지도 화면 내 최대 경도 (동쪽 경계)

    /**
     * 정규화된 이름으로 캐싱된 장소 이미지 URL을 조회합니다.
     */
    @Query("SELECT mi.imageUrl FROM MapPlaceImage mi WHERE mi.normalizedName = :normalizedName")
    Optional<String> findImageUrlByNormalizedName(
            @Param("normalizedName") String normalizedName); // 공백 제거 및 소문자화된 장소 이름 (캐시 키)

    /**
     * 장소 이미지 정보를 저장하거나 업데이트합니다. (Native Query 유지)
     */
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO map_place_image_tb (normalized_name, place_name, image_url, source, created_at)
            VALUES (:normalizedName, :placeName, :imageUrl, :source, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE 
                place_name = :placeName, 
                image_url = :imageUrl, 
                source = :source, 
                created_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    void upsertMapPlaceImage(
            @Param("placeName") String placeName,           // 실제 장소 이름
            @Param("normalizedName") String normalizedName, // 정규화된 장소 이름 (Unique Key)
            @Param("imageUrl") String imageUrl,             // 수집된 이미지 URL
            @Param("source") String source);                // 데이터 출처 (예: 'KAKAO_PLACE')
}
