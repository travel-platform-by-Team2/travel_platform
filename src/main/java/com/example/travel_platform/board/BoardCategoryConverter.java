package com.example.travel_platform.board;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BoardCategoryConverter implements AttributeConverter<BoardCategory, String> {

    @Override
    public String convertToDatabaseColumn(BoardCategory attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public BoardCategory convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return BoardCategory.fromCode(dbData);
    }
}
