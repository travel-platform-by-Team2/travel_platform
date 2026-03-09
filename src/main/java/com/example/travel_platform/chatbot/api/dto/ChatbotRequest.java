package com.example.travel_platform.chatbot.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 챗봇 요청 DTO 모음.
 */
public class ChatbotRequest {

    /**
     * 챗봇 질문 처리 요청 본문.
     */
    @Data
    public static class AskDTO {
        /** 사용자 질문 원문. 공백만 입력하는 값은 허용하지 않는다. */
        @NotBlank
        private String message;

        /** 질문 해석 정확도를 높이기 위한 화면 컨텍스트(선택) */
        @Valid
        private ContextDTO context;
    }

    /**
     * 질문이 발생한 화면 정보를 담는 컨텍스트 DTO.
     */
    @Data
    public static class ContextDTO {
        /** 요청 화면 식별자 (예: booking-list, trip-plan-detail) */
        private String page;
        /** 특정 여행 계획 상세 화면에서 사용하는 식별자 */
        private Integer tripPlanId;
    }
}
