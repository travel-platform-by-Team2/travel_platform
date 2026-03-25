package com.example.travel_platform.trip;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TripRegionConverter implements AttributeConverter<TripRegion, String> {

    @Override
    public String convertToDatabaseColumn(TripRegion attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public TripRegion convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return TripRegion.fromCode(dbData);
    }
}
