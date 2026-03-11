package com.example.travel_platform.seller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SellerSettlementRepository {

    private final EntityManager em;

    public SellerSettlement save(SellerSettlement sellerSettlement) {
        // TODO: 판매자 정산 저장
        return sellerSettlement;
    }

    public Optional<SellerSettlement> findById(Integer settlementId) {
        // TODO: 판매자 정산 단건 조회
        return Optional.empty();
    }

    public List<SellerSettlement> findAllBySellerId(Integer sellerUserId) {
        // TODO: 판매자 정산 목록 조회
        return List.of();
    }
}
