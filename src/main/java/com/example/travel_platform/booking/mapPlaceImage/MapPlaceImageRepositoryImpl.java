package com.example.travel_platform.booking.mapPlaceImage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

class MapPlaceImageRepositoryImpl implements MapPlaceImageRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private final DataSource dataSource;

    MapPlaceImageRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
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
        jakarta.persistence.Query query = entityManager.createNativeQuery("""
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
