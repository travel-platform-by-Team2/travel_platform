package com.example.travel_platform.chatbot.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.example.travel_platform._core.util.Resp;
import com.example.travel_platform.chatbot.api.dto.ChatbotRequest;
import com.example.travel_platform.chatbot.api.dto.ChatbotResponse;
import com.example.travel_platform.chatbot.application.ChatbotOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 챗봇 질문 API 진입점.
 * 현재는 "질문 1건 입력 -> 응답 1건 반환" 단일 요청 방식만 제공한다.
 */
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private static final String ASK_MESSAGES_PATH = "/messages";

    /** 챗봇 질의 처리 오케스트레이션을 담당하는 서비스 */
    private final ChatbotOrchestrator chatbotOrchestrator;

    /**
     * 사용자 질문을 받아 챗봇 처리 결과를 반환한다.
     *
     * @param reqDTO 사용자 질문/컨텍스트 요청 DTO
     * @return 챗봇 처리 결과 DTO
     */
    @PostMapping(ASK_MESSAGES_PATH)
    public ResponseEntity<Resp<ChatbotResponse.AskDTO>> ask(
            @Valid @RequestBody ChatbotRequest.AskDTO reqDTO) {
        return Resp.ok(askResponse(reqDTO));
    }

    private ChatbotResponse.AskDTO askResponse(ChatbotRequest.AskDTO reqDTO) {
        return chatbotOrchestrator.ask(reqDTO);
    }
}

