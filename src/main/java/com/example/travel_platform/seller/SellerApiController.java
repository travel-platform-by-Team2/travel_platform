package com.example.travel_platform.seller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.travel_platform._core.util.Resp;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerApiController {

    private final SellerService sellerService;

    @GetMapping("/dashboard")
    public ResponseEntity<Resp<SellerResponse.DashboardOverviewDTO>> getDashboardOverview() {
        return Resp.ok(sellerService.getDashboardOverview());
    }

    @GetMapping("/products")
    public ResponseEntity<Resp<SellerResponse.ProductListDTO>> getProductList() {
        return Resp.ok(sellerService.getProductList());
    }

    @PostMapping("/products")
    public ResponseEntity<Resp<String>> createProduct(@RequestBody SellerRequest.CreateProductDTO reqDTO) {
        return Resp.ok("상품 등록 API 뼈대가 준비되었습니다.");
    }

    @GetMapping("/bookings")
    public ResponseEntity<Resp<SellerResponse.BookingListDTO>> getBookingList() {
        return Resp.ok(sellerService.getBookingList());
    }

    @PatchMapping("/bookings/{bookingId}/status")
    public ResponseEntity<Resp<String>> updateBookingStatus(
            @PathVariable Integer bookingId,
            @RequestBody SellerRequest.UpdateBookingStatusDTO reqDTO) {

        return Resp.ok("예약 상태 변경 API 뼈대가 준비되었습니다. bookingId=" + bookingId);
    }

    @GetMapping("/settlements")
    public ResponseEntity<Resp<SellerResponse.SettlementListDTO>> getSettlementList() {
        return Resp.ok(sellerService.getSettlementList());
    }
}
