package com.example.travel_platform.trip;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.travel_platform._core.handler.ex.Exception403;
import com.example.travel_platform._core.handler.ex.Exception404;
import com.example.travel_platform.user.User;
import com.example.travel_platform.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class TripService {

    private static final String NOT_IMG = "/images/dumimg.jpg";

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripPlaceRepository tripPlaceRepository;

    @Transactional
    public void createPlan(Integer sessionUserId, TripRequest.CreatePlanDTO reqDTO) {
        User user = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        TripPlan tripPlan = new TripPlan();
        tripPlan.setUser(user);
        tripPlan.setTitle(reqDTO.getTitle());
        tripPlan.setWhoWith(reqDTO.getWhoWith());
        tripPlan.setStartDate(reqDTO.getStartDate());
        tripPlan.setEndDate(reqDTO.getEndDate());
        tripPlan.setImgUrl("placeholder-card.svg");

        tripRepository.savePlan(tripPlan);
    }

    @Transactional
    public void addPlace(Integer sessionUserId, Integer planId, TripRequest.AddPlaceDTO reqDTO) {
        TripPlan tripPlan = tripRepository.findPlanById(planId).orElseThrow();

        TripPlace tripPlace = new TripPlace();
        tripPlace.setTripPlan(tripPlan);
        tripPlace.setPlaceName(reqDTO.getPlaceName());
        tripPlace.setAddress(reqDTO.getAddress());

        tripPlaceRepository.save(tripPlace);
    }

    public TripResponse.PlanListPageDTO getPlanList(Integer userId, String category, int page) {
        int size = 9;
        int offset = page * size;
        int blockSize = 10;
        LocalDate today = LocalDate.now();
        String normalizedCategory = normalizeCategory(category);

        List<TripPlan> tripPlans;
        Long totalCount;

        if ("upcoming".equals(normalizedCategory)) {
            tripPlans = tripRepository.findUpcomingPlanListByUserId(userId, today, offset, size);
            totalCount = tripRepository.countUpcomingPlanByUserId(userId, today);
        } else if ("past".equals(normalizedCategory)) {
            tripPlans = tripRepository.findPastPlanListByUserId(userId, today, offset, size);
            totalCount = tripRepository.countPastPlanByUserId(userId, today);
        } else {
            tripPlans = tripRepository.findPlanListByUserId(userId, offset, size);
            totalCount = tripRepository.countPlanByUserId(userId);
        }

        List<TripResponse.PlanSummaryDTO> plans = new ArrayList<>();

        for (TripPlan tripPlan : tripPlans) {
            String region = regionLabel(tripPlan.getRegion());
            if (region == null || region.isBlank()) {
                region = "지역 정보 없음";
            }

            String imageUrl = tripPlan.getImgUrl();
            if (imageUrl == null || imageUrl.isBlank()) {
                imageUrl = NOT_IMG;
            }

            long diff = ChronoUnit.DAYS.between(today, tripPlan.getStartDate());
            String dDay = "비활성화";
            boolean disabled = true;

            if (diff > 0) {
                dDay = "D-" + diff;
                disabled = false;
            }

            TripResponse.PlanSummaryDTO dto = TripResponse.PlanSummaryDTO.builder()
                    .id(tripPlan.getId())
                    .title(tripPlan.getTitle())
                    .imgUrl(imageUrl)
                    .startDate(tripPlan.getStartDate())
                    .endDate(tripPlan.getEndDate())
                    .placeName(region)
                    .dDay(dDay)
                    .disabled(disabled)
                    .build();

            plans.add(dto);
        }

        int totalPage = (int) Math.ceil((double) totalCount / size);
        int startPage = (page / blockSize) * blockSize;
        int endPage = startPage + blockSize - 1;

        if (endPage >= totalPage) {
            endPage = totalPage - 1;
        }

        List<TripResponse.PageNumberDTO> pageNumbers = new ArrayList<>();
        for (int i = startPage; i <= endPage; i++) {
            pageNumbers.add(new TripResponse.PageNumberDTO(i, i + 1, i == page));
        }

        boolean hasPrev = startPage > 0;
        boolean hasNext = endPage < totalPage - 1;
        int prevPage = startPage - 1;
        int nextPage = endPage + 1;

        return TripResponse.PlanListPageDTO.builder()
                .plans(plans)
                .currentPage(page)
                .displayPage(page + 1)
                .size(size)
                .totalCount(totalCount)
                .totalPage(totalPage)
                .hasPrev(hasPrev)
                .hasNext(hasNext)
                .prevPage(prevPage)
                .nextPage(nextPage)
                .pageNumbers(pageNumbers)
                .startPage(startPage)
                .endPage(endPage)
                .category(normalizedCategory)
                .isResult("result".equals(normalizedCategory))
                .isUpcoming("upcoming".equals(normalizedCategory))
                .isPast("past".equals(normalizedCategory))
                .build();
    }

    public TripResponse.PlanDetailDTO getPlanDetail(Integer sessionUserId, Integer planId) {
        TripPlan tripPlan = tripRepository.findPlanById(planId)
                .orElseThrow(() -> new Exception404("해당 여행 계획을 찾을 수 없습니다."));

        if (!tripPlan.getUser().getId().equals(sessionUserId)) {
            throw new Exception403("권한이 없습니다.");
        }

        List<TripResponse.PlaceDTO> places = new ArrayList<>();
        if (tripPlan.getPlaces() != null) {
            places = tripPlan.getPlaces().stream().map(place -> TripResponse.PlaceDTO.builder()
                    .id(place.getId())
                    .placeName(place.getPlaceName())
                    .address(place.getAddress())
                    .dayOrder(place.getDayOrder())
                    .build()).toList();
        }

        return TripResponse.PlanDetailDTO.builder()
                .id(tripPlan.getId())
                .title(tripPlan.getTitle())
                .region(regionLabel(tripPlan.getRegion()))
                .startDate(tripPlan.getStartDate())
                .endDate(tripPlan.getEndDate())
                .places(places)
                .build();
    }

    private String normalizeCategory(String category) {
        if ("upcoming".equals(category) || "past".equals(category)) {
            return category;
        }
        return "result";
    }

    private String regionLabel(String region) {
        if (region == null || region.isBlank()) {
            return "지역 정보 없음";
        }

        return switch (region) {
            case "seoul" -> "서울";
            case "busan" -> "부산";
            case "daegu" -> "대구";
            case "incheon" -> "인천";
            case "gwangju" -> "광주";
            case "daejeon" -> "대전";
            case "ulsan" -> "울산";
            case "sejong" -> "세종";
            case "gyeonggi" -> "경기도";
            case "gangwon" -> "강원도";
            case "chungbuk" -> "충청북도";
            case "chungnam" -> "충청남도";
            case "jeonbuk" -> "전라북도";
            case "jeonnam" -> "전라남도";
            case "gyeongbuk" -> "경상북도";
            case "gyeongnam" -> "경상남도";
            case "jeju" -> "제주도";
            default -> region;
        };
    }
}
