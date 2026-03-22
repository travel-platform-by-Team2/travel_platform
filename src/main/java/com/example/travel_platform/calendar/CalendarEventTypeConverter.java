package com.example.travel_platform.calendar;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class CalendarEventTypeConverter implements AttributeConverter<CalendarEventType, String> {

    @Override
    public String convertToDatabaseColumn(CalendarEventType attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public CalendarEventType convertToEntityAttribute(String dbData) {
        return CalendarEventType.fromCodeOrNull(dbData);
    }
}
