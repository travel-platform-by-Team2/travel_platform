package com.example.travel_platform.seller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SellerBookingRepository {

    private final EntityManager em;

    public SellerBooking save(SellerBooking sellerBooking) {
        // TODO: 판매자 예약 저장
        return sellerBooking;
    }

    public Optional<SellerBooking> findById(Integer bookingId) {
        // TODO: 판매자 예약 단건 조회
        return Optional.empty();
    }

    public List<SellerBooking> findAllBySellerId(Integer sellerUserId) {
        // TODO: 판매자 예약 목록 조회
        return List.of();
    }
}
