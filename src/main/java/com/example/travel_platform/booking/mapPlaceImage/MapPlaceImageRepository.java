package com.example.travel_platform.booking.mapPlaceImage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MapPlaceImageRepository {

    private final EntityManager em;
    private final DataSource dataSource;

    public Optional<String> findImageUrlByNormalizedName(String normalizedName) {
        return em.createQuery("""
                select mi.imageUrl
                from MapPlaceImage mi
                where mi.normalizedName = :normalizedName
                """, String.class)
                .setParameter("normalizedName", normalizedName)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
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
        } catch (SQLException e) {
            return null;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void upsertWithH2(String normalizedName, String placeName, String imageUrl, String source) {
        jakarta.persistence.Query query = em.createNativeQuery("""
                merge into map_place_image_tb (normalized_name, place_name, image_url, source, created_at)
                key(normalized_name)
                values (?, ?, ?, ?, current_timestamp)
                """);
        query.setParameter(1, normalizedName);
        query.setParameter(2, placeName);
        query.setParameter(3, imageUrl);
        query.setParameter(4, source);
        query.executeUpdate();
    }

    private void upsertWithMySql(String normalizedName, String placeName, String imageUrl, String source) {
        jakarta.persistence.Query query = em.createNativeQuery("""
                insert into map_place_image_tb (normalized_name, place_name, image_url, source, created_at)
                values (?, ?, ?, ?, current_timestamp)
                on duplicate key update
                    place_name = ?,
                    image_url = ?,
                    source = ?,
                    created_at = current_timestamp
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
