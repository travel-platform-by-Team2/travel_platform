package com.example.travel_platform.booking;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LodgingQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<BookingRequest.MapPoiDTO> findActiveLodgingsInBounds(
            String regionKey,
            double minLat,
            double maxLat,
            double minLng,
            double maxLng) {

        String sql = "select external_place_id, name, phone, address, road_address, place_url, category_name, category_group_code, lat, lng " +
                "from lodging_tb " +
                "where is_active = true " +
                "and (? = '' or region_key = ?) " +
                "and lat between ? and ? " +
                "and lng between ? and ?";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    BookingRequest.MapPoiDTO item = new BookingRequest.MapPoiDTO();
                    item.setExternalPlaceId(rs.getString("external_place_id"));
                    item.setName(rs.getString("name"));
                    item.setPhone(rs.getString("phone"));
                    item.setAddress(rs.getString("address"));
                    item.setRoadAddress(rs.getString("road_address"));
                    item.setPlaceUrl(rs.getString("place_url"));
                    item.setCategoryName(rs.getString("category_name"));
                    item.setCategoryGroupCode(rs.getString("category_group_code"));
                    item.setLat(rs.getDouble("lat"));
                    item.setLng(rs.getDouble("lng"));
                    item.setType("hotel");
                    item.setSource("DB");
                    return item;
                },
                regionKey,
                regionKey,
                minLat,
                maxLat,
                minLng,
                maxLng);
    }
}

