package com.example.travel_platform.chatbot.infra.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class OpenAiChatbotLlmClientTest {

    @Test
    void plan() throws Exception {
        OpenAiChatbotLlmClient client = new OpenAiChatbotLlmClient("key", "model", "https://example.com");

        ChatbotLlmPlan plan = (ChatbotLlmPlan) invoke(
                client,
                "parsePlanResponse",
                new Class<?>[] { String.class, String.class },
                """
                        {
                          "needsDb": true,
                          "queryIntent": "USER_BOOKING_LIST",
                          "querySummary": "예약 조회",
                          "sql": "select id from booking_tb limit 5",
                          "answer": ""
                        }
                        """,
                "예약 보여줘");

        assertEquals(true, plan.needsDb());
        assertEquals("USER_BOOKING_LIST", plan.queryIntent());
        assertEquals("예약 조회", plan.querySummary());
        assertEquals("select id from booking_tb limit 5", plan.sql());
    }

    @Test
    void review() throws Exception {
        OpenAiChatbotLlmClient client = new OpenAiChatbotLlmClient("key", "model", "https://example.com");

        ChatbotLlmSearchReview review = (ChatbotLlmSearchReview) invoke(
                client,
                "parseSearchReviewResponse",
                new Class<?>[] { String.class, String.class },
                """
                        {
                          "shouldContinue": true,
                          "queryIntent": "",
                          "querySummary": "제주 재검색",
                          "sql": "select id from board_tb limit 5",
                          "decisionReason": "더 찾아야 한다."
                        }
                        """,
                "BOARD_SEARCH");

        assertEquals(true, review.shouldContinue());
        assertEquals("BOARD_SEARCH", review.queryIntent());
        assertEquals("제주 재검색", review.querySummary());
        assertEquals("select id from board_tb limit 5", review.sql());
    }

    @Test
    void answer() throws Exception {
        OpenAiChatbotLlmClient client = new OpenAiChatbotLlmClient("key", "model", "https://example.com");

        String answer = (String) invoke(
                client,
                "parseDbAnswerResponse",
                new Class<?>[] { String.class },
                """
                        {
                          "answer": "예약이 있습니다."
                        }
                        """);

        assertEquals("예약이 있습니다.", answer);
    }

    @Test
    void text() throws Exception {
        OpenAiChatbotLlmClient client = new OpenAiChatbotLlmClient("key", "model", "https://example.com");

        String text = (String) invoke(
                client,
                "extractOutputText",
                new Class<?>[] { String.class },
                """
                        {
                          "output": [
                            {
                              "content": [
                                {
                                  "type": "output_text",
                                  "text": "{\\"answer\\":\\"ok\\"}"
                                }
                              ]
                            }
                          ]
                        }
                        """);

        assertEquals("{\"answer\":\"ok\"}", text);
    }

    private Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}

