package com.example.travel_platform.booking;

public class BookVar {
    // 예약 시 인원수가 지정되지 않았을 때 사용하는 기본 인원수 (성인 2명)
    public static final int DEFAULT_PERSON_COUNT = 2;

    // 개발 단계에서 테스트용으로 사용하는 기본 사용자 ID (추후 세션 적용 시 대체 대상)
    public static final int DEBUG_USER_ID = 1;

    // 기본 지역 설정값 (데이터 부재 시 폴백용)
    public static final String DEFAULT_REGION_KEY = "busan";
    public static final String DEFAULT_LOCATION_NAME = "부산";
    public static final String DEFAULT_ROOM_NAME = "기본 객실";
}
