package com.example.travel_platform.chatbot;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.travel_platform._core.util.Resp;
import com.example.travel_platform.user.SessionUsers;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotApiController {

    private final ChatbotService chatbotService;

    @PostMapping("/messages")
    public ResponseEntity<Resp<ChatbotResponse.AskDTO>> ask(
            HttpSession session,
            @Valid @RequestBody ChatbotRequest.AskDTO reqDTO) {
        Integer userId = SessionUsers.requireUserId(session);
        return Resp.ok(chatbotService.ask(userId, reqDTO));
    }
}
