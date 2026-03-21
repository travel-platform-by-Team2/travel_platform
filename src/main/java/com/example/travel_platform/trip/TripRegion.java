package com.example.travel_platform.trip;

public enum TripRegion {
    SEOUL("seoul", "서울"),
    BUSAN("busan", "부산"),
    DAEGU("daegu", "대구"),
    INCHEON("incheon", "인천"),
    GWANGJU("gwangju", "광주"),
    DAEJEON("daejeon", "대전"),
    ULSAN("ulsan", "울산"),
    SEJONG("sejong", "세종"),
    GYEONGGI("gyeonggi", "경기"),
    GANGWON("gangwon", "강원"),
    CHUNGBUK("chungbuk", "충북"),
    CHUNGNAM("chungnam", "충남"),
    JEONBUK("jeonbuk", "전북"),
    JEONNAM("jeonnam", "전남"),
    GYEONGBUK("gyeongbuk", "경북"),
    GYEONGNAM("gyeongnam", "경남"),
    JEJU("jeju", "제주");

    private final String code;
    private final String label;

    TripRegion(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static TripRegion fromCode(String code) {
        TripRegion tripRegion = fromCodeOrNull(code);
        if (tripRegion == null) {
            throw new IllegalArgumentException("Unknown trip region code: " + code);
        }
        return tripRegion;
    }

    public static TripRegion fromCodeOrNull(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        for (TripRegion tripRegion : values()) {
            if (tripRegion.code.equals(code)) {
                return tripRegion;
            }
        }
        return null;
    }

    public static boolean isValidCode(String code) {
        return fromCodeOrNull(code) != null;
    }
}
