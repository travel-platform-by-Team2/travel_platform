package com.example.travel_platform.chatbot;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/messages")
    public ChatbotResponse.AskDTO ask(@RequestBody ChatbotRequest.AskDTO reqDTO) {
        return chatbotService.ask(reqDTO);
    }
}
