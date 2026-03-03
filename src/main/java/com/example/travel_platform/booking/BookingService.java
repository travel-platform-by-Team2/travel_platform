package com.example.travel_platform.booking;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    @Transactional
    public void createBooking(Integer sessionUserId, BookingRequest.CreateBookingDTO reqDTO) {
        // TODO: 예약 가능 여부 검증(중복 날짜 등)
        // TODO: 금액/인원 유효성 검증
        // TODO: 엔티티 변환 후 저장
    }

    @Transactional
    public void cancelBooking(Integer sessionUserId, Integer bookingId) {
        // TODO: 소유권 검증
        // TODO: 취소 정책 반영
    }

    public List<BookingResponse.BookingSummaryDTO> getBookingList(Integer sessionUserId) {
        // TODO: 사용자 예약 목록 조회
        // TODO: BookingSummaryDTO 매핑
        return List.of();
    }

    public BookingResponse.BookingDetailDTO getBookingDetail(Integer sessionUserId, Integer bookingId) {
        // TODO: 상세 조회 + 소유권 검증
        // TODO: BookingDetailDTO 매핑
        return null;
    }
}

