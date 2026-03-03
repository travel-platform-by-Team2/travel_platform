package com.example.travel_platform.booking;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BookingRepository {

    private final EntityManager em;

    public Booking save(Booking booking) {
        // TODO: 예약 저장 처리
        return booking;
    }

    public Optional<Booking> findById(Integer bookingId) {
        // TODO: 예약 단건 조회 구현
        return Optional.empty();
    }

    public List<Booking> findListByUserId(Integer userId) {
        // TODO: 사용자별 예약 목록 조회 구현
        return List.of();
    }
}

