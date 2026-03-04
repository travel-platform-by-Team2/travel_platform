package com.example.travel_platform.chatbot;

import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    public ChatbotResponse.AskDTO ask(ChatbotRequest.AskDTO reqDTO) {
        String message = reqDTO.getMessage() == null ? "" : reqDTO.getMessage().trim();
        ClassificationResult classification = classifyQuestion(message);
        String processingType = classification.needsDb ? "DB_QUERY" : "DIRECT_LLM";
        String answer = buildPlaceholderAnswer(classification);

        return ChatbotResponse.AskDTO.builder()
                .processingType(processingType)
                .answer(answer)
                .meta(ChatbotResponse.MetaDTO.builder()
                        .needsDb(classification.needsDb)
                        .build())
                .build();
    }

    // 4-3 단계: 질문 분류 결과(needsDb/reason/queryIntent)를 산출한다.
    private ClassificationResult classifyQuestion(String message) {
        String normalized = message.toLowerCase();

        if (normalized.contains("예약")) {
            return new ClassificationResult(true, "예약 데이터 조회가 필요함", "USER_BOOKING_LIST");
        }
        if (normalized.contains("일정")) {
            return new ClassificationResult(true, "캘린더 일정 데이터 조회가 필요함", "USER_CALENDAR_LIST");
        }
        if (normalized.contains("게시글")) {
            return new ClassificationResult(true, "커뮤니티 데이터 조회가 필요함", "COMMUNITY_POST_INFO");
        }
        if (normalized.contains("여행") || normalized.contains("코스")) {
            return new ClassificationResult(true, "여행 계획/장소 데이터 조회가 필요함", "TRIP_PLAN_INFO");
        }

        return new ClassificationResult(false, "일반 대화 질문으로 DB 조회 불필요", "GENERAL_CHAT");
    }

    // TODO: 4-4, 4-5 단계에서 SQL/조회/최종 답변 로직으로 교체한다.
    private String buildPlaceholderAnswer(ClassificationResult classification) {
        if (classification.needsDb) {
            return "질문을 확인했어요. (" + classification.queryIntent + ") 현재는 DB 연계 전 임시 답변을 제공하고 있어요.";
        }
        return "질문을 확인했어요. 현재는 임시 답변 단계입니다.";
    }

    private static class ClassificationResult {
        private final boolean needsDb;
        private final String reason;
        private final String queryIntent;

        private ClassificationResult(boolean needsDb, String reason, String queryIntent) {
            this.needsDb = needsDb;
            this.reason = reason;
            this.queryIntent = queryIntent;
        }
    }
}
