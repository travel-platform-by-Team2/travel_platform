package com.example.travel_platform.seller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/seller")
public class SellerController {

    @GetMapping
    public String dashboardPage(Model model) {
        applyLayout(
                model,
                "dashboard",
                "판매자 대시보드",
                "오늘 운영 현황과 정산 예정 정보를 한눈에 확인하세요.",
                null,
                null);
        return "pages/seller-dashboard";
    }

    @GetMapping("/products")
    public String productsPage(Model model) {
        applyLayout(
                model,
                "products",
                "상품관리",
                "등록한 상품의 노출 상태와 운영 준비 현황을 관리하세요.",
                "신규 상품 등록",
                "#");
        return "pages/seller-products";
    }

    @GetMapping("/bookings")
    public String bookingsPage(Model model) {
        applyLayout(
                model,
                "bookings",
                "예약관리",
                "예약 상태와 게스트 일정을 빠르게 확인할 수 있도록 화면만 먼저 준비했습니다.",
                null,
                null);
        return "pages/seller-bookings";
    }

    @GetMapping("/settlements")
    public String settlementsPage(Model model) {
        applyLayout(
                model,
                "settlements",
                "정산내역",
                "월별 정산 흐름과 정책 안내를 같은 화면에서 볼 수 있도록 정리했습니다.",
                "정산 가이드",
                "#");
        return "pages/seller-settlements";
    }

    private void applyLayout(
            Model model,
            String section,
            String title,
            String description,
            String actionText,
            String actionHref) {

        model.addAttribute("sellerPageTitle", title);
        model.addAttribute("sellerPageDescription", description);
        model.addAttribute("sellerPageActionText", actionText);
        model.addAttribute("sellerPageActionHref", actionHref);
        model.addAttribute("sellerSidebarEmail", "seller@example.com");
        model.addAttribute("sellerSidebarGreeting", "안녕하세요 관리자님");
        model.addAttribute("sellerDashboardActive", "dashboard".equals(section));
        model.addAttribute("sellerProductsActive", "products".equals(section));
        model.addAttribute("sellerBookingsActive", "bookings".equals(section));
        model.addAttribute("sellerSettlementsActive", "settlements".equals(section));
    }
}
