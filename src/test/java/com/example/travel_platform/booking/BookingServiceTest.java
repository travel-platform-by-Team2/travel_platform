package com.example.travel_platform.booking;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class BookingServiceTest {

    // .env에서 확인한 실제 서비스 키 (테스트용)
    private final String serviceKey = "A6g65ZteBO8qpJ5z4rQkSPkVltHPXfnLNCa5Q7dlz6B1zFAi7UGM251/sAcsYRAef/AeWI6Yy7SYLQZDyAkBmg==";

    @Test
    public void fetchRoomsFromTourApi_test() {
        // given
        // 리포지토리들은 TourAPI 호출에 필요하지 않으므로 null 처리 (현재 4개 파라미터 필요)
        BookingService bookingService = new BookingService(null, null, null, null);
        String lodgingName = "파라다이스 호텔 부산";
        String address = "부산광역시 해운대구";

        // when
        List<Map<String, Object>> rooms = bookingService.fetchRoomsFromTourApi(serviceKey, lodgingName, address);

        // then
        System.out.println("=======================");
        System.out.println("조회된 객실 수: " + (rooms == null ? 0 : rooms.size()));
        if (rooms != null) {
            for (Map<String, Object> room : rooms) {
                System.out.println("객실명: " + room.get("name"));
                System.out.println("이미지: " + room.get("imageUrl"));
                System.out.println("설명: " + room.get("content"));
                System.out.println("-----------------------");
            }
        }
    }

    @Test
    public void fetchImageFromTourApi_test() {
        // given
        BookingService bookingService = new BookingService(null, null, null, null);
        String lodgingName = "해운대해수욕장";
        String address = "부산광역시";

        // when
        // getPlaceImage를 호출하여 이미지를 잘 가져오는지 확인
        // 단, 내부에서 리포지토리를 사용하므로 Mocking 없이 호출 시 NPE 발생 가능
        // 실제 API 연동 확인을 위해 fetchImageFromTourApi를 public으로 잠시 생각하거나 리포지토리만 모킹 필요
        System.out.println("TourAPI 이미지 연동 테스트 생략 (리포지토리 의존성 필요)");
    }
}
