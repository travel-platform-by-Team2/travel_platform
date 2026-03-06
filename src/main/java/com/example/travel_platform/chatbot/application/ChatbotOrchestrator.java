package com.example.travel_platform.chatbot.application;

import com.example.travel_platform.chatbot.api.dto.ChatbotRequest;
import com.example.travel_platform.chatbot.api.dto.ChatbotResponse;
import com.example.travel_platform.chatbot.infra.llm.ChatbotLlmPlan;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * 챗봇 질문 처리 메인 서비스.
 *
 * 처리 흐름:
 * 1) 사용자 질문 정규화
 * 2) LLM 1차 계획 수립 (needsDb, queryIntent, querySummary, sql, answer)
 * 3) DB 필요 여부 분기
 *    - false: LLM이 생성한 direct answer 반환
 *    - true: SQL 실행 후 DB 결과를 LLM에 전달해 최종 답변 생성
 */
@Service
@RequiredArgsConstructor
public class ChatbotOrchestrator {

    private final ChatbotPlanService chatbotPlanService;
    private final ChatbotQueryService chatbotQueryService;
    private final ChatbotAnswerService chatbotAnswerService;

    /**
     * 질문 1건을 처리한다.
     *
     * @param reqDTO 사용자 질문 요청
     * @return 처리 결과(DIRECT_LLM 또는 DB_QUERY)
     */
    public ChatbotResponse.AskDTO ask(ChatbotRequest.AskDTO reqDTO) {
        String message = sanitize(reqDTO.getMessage());
        ChatbotLlmPlan llmPlan = chatbotPlanService.createPlan(message, reqDTO.getContext());

        // DB가 필요 없으면 1차 계획의 answer를 그대로 최종 응답으로 사용한다.
        if (!llmPlan.needsDb()) {
            return ChatbotResponse.AskDTO.builder()
                    .processingType("DIRECT_LLM")
                    .answer(chatbotAnswerService.resolveDirectAnswer(llmPlan))
                    .meta(ChatbotResponse.MetaDTO.builder()
                            .needsDb(false)
                            .build())
                    .build();
        }

        // DB가 필요하면 SQL 실행 결과를 바탕으로 LLM 2차 답변을 생성한다.
        ChatbotQueryService.QueryResult queryResult = chatbotQueryService.execute(llmPlan);
        String answer = chatbotAnswerService.createDbAnswer(message, llmPlan, queryResult.rows());

        return ChatbotResponse.AskDTO.builder()
                .processingType("DB_QUERY")
                .answer(answer)
                .meta(ChatbotResponse.MetaDTO.builder()
                        .needsDb(true)
                        .querySummary(queryResult.querySummary())
                        .generatedSql(queryResult.sql())
                        .rowCount(queryResult.rows().size())
                        .build())
                .build();
    }

    /**
     * 질문 문자열을 정규화한다.
     * null 입력을 방어하고 좌우 공백을 제거한다.
     */
    private String sanitize(String message) {
        return message == null ? "" : message.trim();
    }
}
