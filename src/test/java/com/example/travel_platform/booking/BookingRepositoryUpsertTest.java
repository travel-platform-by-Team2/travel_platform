package com.example.travel_platform.booking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

class BookingRepositoryUpsertTest {

    @Test
    void h2_merge_into_works() throws Exception {
        String jdbcUrl = "jdbc:h2:mem:merge_test;DB_CLOSE_DELAY=-1";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, "sa", "")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE map_place_image_tb (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            normalized_name VARCHAR(200) NOT NULL,
                            place_name VARCHAR(200) NOT NULL,
                            image_url VARCHAR(2000) NOT NULL,
                            source VARCHAR(50) NOT NULL,
                            created_at TIMESTAMP NOT NULL,
                            UNIQUE (normalized_name)
                        )
                        """);
            }

            String upsertSql = """
                    MERGE INTO map_place_image_tb (normalized_name, place_name, image_url, source, created_at)
                    KEY(normalized_name)
                    VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                    """;

            try (PreparedStatement preparedStatement = connection.prepareStatement(upsertSql)) {
                preparedStatement.setString(1, "testplace");
                preparedStatement.setString(2, "place1");
                preparedStatement.setString(3, "https://img/1");
                preparedStatement.setString(4, "SRC1");
                preparedStatement.executeUpdate();
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(upsertSql)) {
                preparedStatement.setString(1, "testplace");
                preparedStatement.setString(2, "place2");
                preparedStatement.setString(3, "https://img/2");
                preparedStatement.setString(4, "SRC2");
                preparedStatement.executeUpdate();
            }

            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("""
                        SELECT COUNT(*) AS cnt, place_name, image_url, source
                        FROM map_place_image_tb
                        GROUP BY place_name, image_url, source
                        """)) {
                    assertEquals(true, resultSet.next());
                    assertEquals(1, resultSet.getInt("cnt"));
                    assertEquals("place2", resultSet.getString("place_name"));
                    assertEquals("https://img/2", resultSet.getString("image_url"));
                    assertEquals("SRC2", resultSet.getString("source"));
                }
            }
        }
    }

    @Test
    void h2MySqlMode_onDuplicateKeyUpdate_works() throws Exception {
        String jdbcUrl = "jdbc:h2:mem:upsert_test;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, "sa", "")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE map_place_image_tb (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            normalized_name VARCHAR(200) NOT NULL,
                            place_name VARCHAR(200) NOT NULL,
                            image_url VARCHAR(2000) NOT NULL,
                            source VARCHAR(50) NOT NULL,
                            created_at TIMESTAMP NOT NULL,
                            UNIQUE (normalized_name)
                        )
                        """);
            }

            String upsertSql = """
                    INSERT INTO map_place_image_tb (normalized_name, place_name, image_url, source, created_at)
                    VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
                    ON DUPLICATE KEY UPDATE
                        place_name = ?,
                        image_url = ?,
                        source = ?,
                        created_at = CURRENT_TIMESTAMP
                    """;

            try (PreparedStatement preparedStatement = connection.prepareStatement(upsertSql)) {
                preparedStatement.setString(1, "testplace");
                preparedStatement.setString(2, "place1");
                preparedStatement.setString(3, "https://img/1");
                preparedStatement.setString(4, "SRC1");
                preparedStatement.setString(5, "place1");
                preparedStatement.setString(6, "https://img/1");
                preparedStatement.setString(7, "SRC1");
                preparedStatement.executeUpdate();
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(upsertSql)) {
                preparedStatement.setString(1, "testplace");
                preparedStatement.setString(2, "place2");
                preparedStatement.setString(3, "https://img/2");
                preparedStatement.setString(4, "SRC2");
                preparedStatement.setString(5, "place2");
                preparedStatement.setString(6, "https://img/2");
                preparedStatement.setString(7, "SRC2");
                preparedStatement.executeUpdate();
            }

            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery("""
                        SELECT COUNT(*) AS cnt, place_name, image_url, source
                        FROM map_place_image_tb
                        GROUP BY place_name, image_url, source
                        """)) {
                    assertEquals(true, resultSet.next());
                    assertEquals(1, resultSet.getInt("cnt"));
                    assertEquals("place2", resultSet.getString("place_name"));
                    assertEquals("https://img/2", resultSet.getString("image_url"));
                    assertEquals("SRC2", resultSet.getString("source"));
                }
            }
        }
    }
}
