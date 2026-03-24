package com.example.travel_platform.weather;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

@Getter
public enum WeatherRegion {
    SEOUL("seoul", "\uC11C\uC6B8", "11B00000", "11B10101", 60, 127, List.of("\uC11C\uC6B8\uC2DC", "\uC11C\uC6B8\uD2B9\uBCC4\uC2DC")),
    BUSAN("busan", "\uBD80\uC0B0", "11H20000", "11H20201", 98, 76, List.of("\uBD80\uC0B0\uC2DC", "\uBD80\uC0B0\uAD11\uC5ED\uC2DC")),
    DAEGU("daegu", "\uB300\uAD6C", "11H10000", "11H10701", 89, 90, List.of("\uB300\uAD6C\uC2DC", "\uB300\uAD6C\uAD11\uC5ED\uC2DC")),
    INCHEON("incheon", "\uC778\uCC9C", "11B00000", "11B20201", 55, 124, List.of("\uC778\uCC9C\uC2DC", "\uC778\uCC9C\uAD11\uC5ED\uC2DC")),
    GWANGJU("gwangju", "\uAD11\uC8FC", "11F20000", "11F20501", 58, 74, List.of("\uAD11\uC8FC\uC2DC", "\uAD11\uC8FC\uAD11\uC5ED\uC2DC")),
    DAEJEON("daejeon", "\uB300\uC804", "11C20000", "11C20401", 67, 100, List.of("\uB300\uC804\uC2DC", "\uB300\uC804\uAD11\uC5ED\uC2DC")),
    ULSAN("ulsan", "\uC6B8\uC0B0", "11H20000", "11H20101", 102, 84, List.of("\uC6B8\uC0B0\uC2DC", "\uC6B8\uC0B0\uAD11\uC5ED\uC2DC")),
    SEJONG("sejong", "\uC138\uC885", "11C20000", "11C20404", 66, 103, List.of("\uC138\uC885\uC2DC", "\uC138\uC885\uD2B9\uBCC4\uC790\uCE58\uC2DC")),
    GYEONGGI("gyeonggi", "\uACBD\uAE30", "11B00000", "11B20601", 60, 120, List.of("\uACBD\uAE30\uB3C4", "\uACBD\uAE30")),
    GANGWON("gangwon", "\uAC15\uC6D0", "11D10000", "11D10301", 73, 134, List.of("\uAC15\uC6D0\uB3C4", "\uAC15\uC6D0\uD2B9\uBCC4\uC790\uCE58\uB3C4")),
    CHUNGBUK("chungbuk", "\uCDA9\uBD81", "11C10000", "11C10301", 69, 107, List.of("\uCDA9\uCCAD\uBD81\uB3C4", "\uCDA9\uBD81")),
    CHUNGNAM("chungnam", "\uCDA9\uB0A8", "11C20000", "11C20301", 55, 107, List.of("\uCDA9\uCCAD\uB0A8\uB3C4", "\uCDA9\uB0A8")),
    JEONBUK("jeonbuk", "\uC804\uBD81", "11F10000", "11F10201", 63, 89, List.of("\uC804\uB77C\uBD81\uB3C4", "\uC804\uBD81", "\uC804\uBD81\uD2B9\uBCC4\uC790\uCE58\uB3C4")),
    JEONNAM("jeonnam", "\uC804\uB0A8", "11F20000", "11F20503", 51, 67, List.of("\uC804\uB77C\uB0A8\uB3C4", "\uC804\uB0A8")),
    GYEONGBUK("gyeongbuk", "\uACBD\uBD81", "11H10000", "11H10501", 87, 106, List.of("\uACBD\uC0C1\uBD81\uB3C4", "\uACBD\uBD81")),
    GYEONGNAM("gyeongnam", "\uACBD\uB0A8", "11H20000", "11H20301", 91, 77, List.of("\uACBD\uC0C1\uB0A8\uB3C4", "\uACBD\uB0A8")),
    JEJU("jeju", "\uC81C\uC8FC", "11G00000", "11G00201", 52, 38, List.of("\uC81C\uC8FC\uB3C4", "\uC81C\uC8FC\uD2B9\uBCC4\uC790\uCE58\uB3C4"));

    private final String code;
    private final String label;
    private final String landRegId;
    private final String temperatureRegId;
    private final int shortTermNx;
    private final int shortTermNy;
    private final List<String> aliases;

    WeatherRegion(
            String code,
            String label,
            String landRegId,
            String temperatureRegId,
            int shortTermNx,
            int shortTermNy,
            List<String> aliases) {
        this.code = code;
        this.label = label;
        this.landRegId = landRegId;
        this.temperatureRegId = temperatureRegId;
        this.shortTermNx = shortTermNx;
        this.shortTermNy = shortTermNy;
        this.aliases = aliases;
    }

    public static WeatherRegion fromInput(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String normalized = input.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(region -> region.matches(normalized))
                .findFirst()
                .orElse(null);
    }

    private boolean matches(String normalized) {
        if (code.equals(normalized)) {
            return true;
        }
        if (label.equals(normalized)) {
            return true;
        }
        return aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(normalized));
    }
}
