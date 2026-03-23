package com.example.travel_platform.trip;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TripCompanionTypeConverter implements AttributeConverter<TripCompanionType, String> {

    @Override
    public String convertToDatabaseColumn(TripCompanionType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public TripCompanionType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return TripCompanionType.fromCode(dbData);
    }
}
