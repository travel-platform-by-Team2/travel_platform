package com.example.travel_platform.chatbot.infra.llm;

import java.util.List;
import java.util.Map;

import com.example.travel_platform.chatbot.api.dto.ChatbotRequest;

/**
 * 챗봇과 LLM 연동 계약 인터페이스.
 * 서비스는 이 인터페이스만 의존하고, 실제 구현(OpenAI 등)은 교체 가능하도록 분리한다.
 */
public interface ChatbotLlmClient {

    /**
     * 사용자 질문에 대한 1차 실행 계획을 생성한다.
     *
     * @param userMessage 사용자 질문
     * @param context 화면 컨텍스트
     * @param schemaContext 테이블/컬럼 스키마 정보(JSON 문자열)
     * @return DB 필요 여부, SQL, direct answer 등을 포함한 계획 객체
     */
    ChatbotLlmPlan createPlan(String userMessage, ChatbotRequest.ContextDTO context, String schemaContext);

    /**
     * DB 조회 결과를 바탕으로 최종 사용자 답변을 생성한다.
     *
     * @param userMessage 사용자 질문 원문
     * @param queryIntent 1차 계획에서 계산한 질의 의도
     * @param queryRows DB 조회 결과 row 목록
     * @return 사용자에게 전달할 최종 자연어 답변
     */
    String createDbAnswer(String userMessage, String queryIntent, List<Map<String, Object>> queryRows);
}
