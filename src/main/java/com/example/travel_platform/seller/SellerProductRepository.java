package com.example.travel_platform.seller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SellerProductRepository {

    private final EntityManager em;

    public SellerProduct save(SellerProduct sellerProduct) {
        // TODO: 판매자 상품 저장
        return sellerProduct;
    }

    public Optional<SellerProduct> findById(Integer productId) {
        // TODO: 판매자 상품 단건 조회
        return Optional.empty();
    }

    public List<SellerProduct> findAllBySellerId(Integer sellerUserId) {
        // TODO: 판매자 상품 목록 조회
        return List.of();
    }
}
