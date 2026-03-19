package com.example.travel_platform.booking;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * 예약(Booking) 엔티티에 대한 기본 CRUD와 더불어,
 * 숙소(Lodging) 조회 및 장소 이미지 캐싱 기능을 통합 관리하는 유일한 리포지토리입니다.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

        /**
         * 특정 지도 경계 영역 내의 활성화된 숙소 목록을 조회하여 DTO로 즉시 반환합니다.
         */
        @Query("""
                        SELECT new com.example.travel_platform.booking.BookingResponse$MapPoiDTO(
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
        List<BookingResponse.MapPoiDTO> findActiveLodgingsInBounds(
                        @Param("regionKey") String regionKey,
                        @Param("minLat") double minLat,
                        @Param("maxLat") double maxLat,
                        @Param("minLng") double minLng,
                        @Param("maxLng") double maxLng);

        /**
         * 정규화된 이름으로 캐싱된 장소 이미지 URL을 조회합니다.
         */
        @Query("SELECT mi.imageUrl FROM MapPlaceImage mi WHERE mi.normalizedName = :normalizedName")
        Optional<String> findImageUrlByNormalizedName(@Param("normalizedName") String normalizedName);

        /**
         * 장소 이미지 정보를 저장하거나 업데이트합니다 (Upsert).
         * H2/MySQL은 업서트 문법이 달라서, 같은 파일의 {@code BookingRepositoryImpl}에서 DB별로 분기합니다.
         */
        void upsertMapPlaceImage(String normalizedName, String placeName, String imageUrl, String source);


}

class BookingRepositoryImpl {

    @PersistenceContext
    private EntityManager entityManager;

    private final DataSource dataSource;

    BookingRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Transactional
    public void upsertMapPlaceImage(String normalizedName, String placeName, String imageUrl, String source) {
        String databaseProductName = resolveDatabaseProductName();
        if (databaseProductName != null && databaseProductName.toLowerCase().contains("mysql")) {
            upsertWithMySql(normalizedName, placeName, imageUrl, source);
            return;
        }
        upsertWithH2(normalizedName, placeName, imageUrl, source);
    }

    private String resolveDatabaseProductName() {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            return metaData.getDatabaseProductName();
        } catch (Exception e) {
            return null;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void upsertWithH2(String normalizedName, String placeName, String imageUrl, String source) {
        jakarta.persistence.Query query = entityManager.createNativeQuery("""
                MERGE INTO map_place_image_tb (normalized_name, place_name, image_url, source, created_at)
                KEY(normalized_name)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                """);
        query.setParameter(1, normalizedName);
        query.setParameter(2, placeName);
        query.setParameter(3, imageUrl);
        query.setParameter(4, source);
        query.executeUpdate();
    }

    private void upsertWithMySql(String normalizedName, String placeName, String imageUrl, String source) {
        jakarta.persistence.Query query = entityManager.createNativeQuery("""
                INSERT INTO map_place_image_tb (normalized_name, place_name, image_url, source, created_at)
                VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                    place_name = ?,
                    image_url = ?,
                    source = ?,
                    created_at = CURRENT_TIMESTAMP
                """);
        query.setParameter(1, normalizedName);
        query.setParameter(2, placeName);
        query.setParameter(3, imageUrl);
        query.setParameter(4, source);
        query.setParameter(5, placeName);
        query.setParameter(6, imageUrl);
        query.setParameter(7, source);
        query.executeUpdate();
    }
}
