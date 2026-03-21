package com.example.travel_platform.trip;

public enum TripCompanionType {
    SOLO("solo", "혼자"),
    FRIEND("friend", "친구와"),
    FAMILY("family", "가족과"),
    COUPLE("couple", "연인과");

    private final String code;
    private final String label;

    TripCompanionType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static TripCompanionType fromCode(String code) {
        TripCompanionType companionType = fromCodeOrNull(code);
        if (companionType == null) {
            throw new IllegalArgumentException("Unknown companion type code: " + code);
        }
        return companionType;
    }

    public static TripCompanionType fromCodeOrNull(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        for (TripCompanionType companionType : values()) {
            if (companionType.code.equals(code)) {
                return companionType;
            }
        }
        return null;
    }

    public static boolean isValidCode(String code) {
        return fromCodeOrNull(code) != null;
    }
}
