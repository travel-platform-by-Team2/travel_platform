package com.example.travel_platform.trip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.example.travel_platform._core.validation.ValidEnumCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TripRequest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePlanDTO {
        @NotBlank(message = "여행 제목을 입력해주세요.")
        private String title;

        @NotBlank(message = "누구와 함께 가는 여행인지 선택해주세요.")
        @ValidEnumCode(enumClass = TripCompanionType.class, message = "유효한 동행 유형을 선택해주세요.")
        private String whoWith;

        @NotBlank(message = "여행 지역을 선택해주세요.")
        @ValidEnumCode(enumClass = TripRegion.class, message = "유효한 여행 지역을 선택해주세요.")
        private String region;

        @NotNull(message = "여행 시작일을 선택해주세요.")
        private LocalDate startDate;

        @NotNull(message = "여행 종료일을 선택해주세요.")
        private LocalDate endDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddPlaceDTO {
        @NotBlank(message = "장소 이름이 필요합니다.")
        private String placeName;

        @NotBlank(message = "주소 정보가 필요합니다.")
        private String address;

        @NotNull(message = "위도 정보가 필요합니다.")
        private BigDecimal latitude;

        @NotNull(message = "경도 정보가 필요합니다.")
        private BigDecimal longitude;

        @NotNull(message = "여행 일차 정보가 필요합니다.")
        private Integer dayOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddPlacesDTO {
        private Integer tripDay;
        private List<PlaceDTO> places;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceDTO {
        private String placeName;
        private String address;
        private Double latitude;
        private Double longitude;
        private String placeUrl;
        private String imgUrl;
        private String type;
    }
}
