package com.example.travel_platform.chatbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.travel_platform._core.handler.ex.ApiException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
public class ChatbotLlmClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_RECENT_HISTORY_ITEMS = 8;

    private static final String INTERPRET_SYSTEM_PROMPT = """
            당신은 여행 플랫폼 챗봇의 질문 해석기다.
            SQL은 생성하지 말고, 현재 질문과 history를 바탕으로 어떤 처리 모드와 어떤 조회 계획이 필요한지만 JSON으로 판단한다.
            반드시 아래 JSON 객체 형식으로만 응답하라.

            {
              "mode": "DB_QA" | "GENERAL_CHAT",
              "resolvedContext": {
                "domain": "BOOKING" | "TRIP" | "CALENDAR" | "BOARD" | "WEATHER" | null,
                "intent": string | null,
                "region": string | null,
                "targetDate": "YYYY-MM-DD" | null,
                "endDate": "YYYY-MM-DD" | null,
                "keyword": string | null,
                "category": string | null,
                "tripPlanId": number | null,
                "limit": number | null,
                "isFollowUp": boolean,
                "missingFields": [string]
              },
              "queryPlans": [
                {
                  "domain": "BOOKING" | "TRIP" | "CALENDAR" | "BOARD" | "WEATHER",
                  "intent": string,
                  "keyword": string,
                  "startDate": "YYYY-MM-DD",
                  "endDate": "YYYY-MM-DD",
                  "category": string,
                  "limit": number
                }
              ]
            }

            규칙:
            - 내부 데이터 조회나 날씨 조회가 필요하면 mode는 DB_QA로 둔다.
            - 인사, 잡담, 일반 설명은 GENERAL_CHAT으로 둔다.
            - resolvedContext는 현재 질문과 history를 종합한 공통 슬롯 결과다.
            - 이전 대화의 정보를 이어받는 후속 질문이면 isFollowUp을 true로 둔다.
            - 현재 답변이나 조회를 위해 아직 부족한 값이 있으면 missingFields에 넣는다.
            - queryPlans는 최대 3개까지만 반환한다.
            - WEATHER는 intent를 DATE_FORECAST로 두고, keyword에는 지역, startDate에는 기준 날짜를 넣는다.
            - markdown, 코드 블록, 설명 문장은 출력하지 않는다.
            """;

    private static final String GENERAL_CHAT_SYSTEM_PROMPT = """
            당신은 여행 플랫폼의 친절한 챗봇이다.
            사용자의 질문에 자연스럽고 간결한 한국어로 답변한다.
            내부 처리 과정, SQL, 검증 규칙은 드러내지 않는다.
            """;

    private static final String DB_ANSWER_SYSTEM_PROMPT = """
            당신은 여행 플랫폼의 챗봇이다.
            사용자의 질문, 질문 해석 결과, 조회 결과 블록을 바탕으로 자연스럽고 간결한 한국어 답변을 만든다.
            조회 도구 이름, SQL, 내부 처리 과정은 드러내지 않는다.
            """;

    private static final Gson GSON = new Gson();

    private final String apiKey;
    private final String model;
    private final String endpoint;

    public ChatbotLlmClient(
            @Value("${OPENAI_API_KEY:}") String apiKey,
            @Value("${OPENAI_MODEL:gpt-4.1-mini}") String model,
            @Value("${OPENAI_ENDPOINT:https://api.openai.com/v1/responses}") String endpoint) {
        this.apiKey = apiKey;
        this.model = model;
        this.endpoint = endpoint;
    }

    public ChatbotService.Interpretation interpret(String userMessage, ChatbotRequest.ContextDTO context, String toolContext) {
        return interpret(userMessage, context, List.of(), toolContext);
    }

    public ChatbotService.Interpretation interpret(
            String userMessage,
            ChatbotRequest.ContextDTO context,
            List<ChatbotRequest.HistoryItemDTO> history,
            String toolContext) {
        try {
            String response = callOpenAi(
                    INTERPRET_SYSTEM_PROMPT,
                    buildInterpretUserPrompt(userMessage, context, history, toolContext));
            return parseInterpretation(response);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "LLM 질문 해석 중 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    public String answerGeneralChat(String userMessage, ChatbotRequest.ContextDTO context) {
        return answerGeneralChat(userMessage, context, List.of());
    }

    public String answerGeneralChat(
            String userMessage,
            ChatbotRequest.ContextDTO context,
            List<ChatbotRequest.HistoryItemDTO> history) {
        return callOpenAi(GENERAL_CHAT_SYSTEM_PROMPT, buildGeneralAnswerUserPrompt(userMessage, context, history));
    }

    public String answerDbQa(
            String userMessage,
            ChatbotRequest.ContextDTO context,
            ChatbotService.Interpretation interpretation,
            List<ChatbotService.QueryBlock> queryBlocks) {
        return answerDbQa(userMessage, context, List.of(), interpretation, queryBlocks);
    }

    public String answerDbQa(
            String userMessage,
            ChatbotRequest.ContextDTO context,
            List<ChatbotRequest.HistoryItemDTO> history,
            ChatbotService.Interpretation interpretation,
            List<ChatbotService.QueryBlock> queryBlocks) {
        return callOpenAi(
                DB_ANSWER_SYSTEM_PROMPT,
                buildDbAnswerUserPrompt(userMessage, context, history, interpretation, queryBlocks));
    }

    private String buildInterpretUserPrompt(
            String userMessage,
            ChatbotRequest.ContextDTO context,
            List<ChatbotRequest.HistoryItemDTO> history,
            String toolContext) {
        List<ChatbotRequest.HistoryItemDTO> sanitizedHistory = sanitizeHistory(history);
        JsonObject promptJson = new JsonObject();
        promptJson.addProperty("message", userMessage);
        promptJson.add("context", buildContextJson(context));
        promptJson.add("history", GSON.toJsonTree(sanitizedHistory));
        promptJson.addProperty("recentConversation", buildRecentConversation(sanitizedHistory));
        promptJson.addProperty(
                "followUpInstruction",
                "If the current message omits region, date, keyword, or topic, infer them from recentConversation when the conversation clearly continues.");
        promptJson.add("toolContext", toJsonElement(toolContext));
        return GSON.toJson(promptJson);
    }

    private String buildGeneralAnswerUserPrompt(
            String userMessage,
            ChatbotRequest.ContextDTO context,
            List<ChatbotRequest.HistoryItemDTO> history) {
        List<ChatbotRequest.HistoryItemDTO> sanitizedHistory = sanitizeHistory(history);
        JsonObject promptJson = new JsonObject();
        promptJson.addProperty("message", userMessage);
        promptJson.add("context", buildContextJson(context));
        promptJson.add("history", GSON.toJsonTree(sanitizedHistory));
        promptJson.addProperty("recentConversation", buildRecentConversation(sanitizedHistory));
        return GSON.toJson(promptJson);
    }

    private String buildDbAnswerUserPrompt(
            String userMessage,
            ChatbotRequest.ContextDTO context,
            List<ChatbotRequest.HistoryItemDTO> history,
            ChatbotService.Interpretation interpretation,
            List<ChatbotService.QueryBlock> queryBlocks) {
        List<ChatbotRequest.HistoryItemDTO> sanitizedHistory = sanitizeHistory(history);
        JsonObject promptJson = new JsonObject();
        promptJson.addProperty("message", userMessage);
        promptJson.add("context", buildContextJson(context));
        promptJson.add("history", GSON.toJsonTree(sanitizedHistory));
        promptJson.addProperty("recentConversation", buildRecentConversation(sanitizedHistory));
        promptJson.add("interpretation", GSON.toJsonTree(interpretation));
        promptJson.add("queryBlocks", GSON.toJsonTree(queryBlocks));
        return GSON.toJson(promptJson);
    }

    private JsonObject buildContextJson(ChatbotRequest.ContextDTO context) {
        JsonObject contextJson = new JsonObject();
        if (context == null) {
            return contextJson;
        }
        if (context.getPage() != null && !context.getPage().isBlank()) {
            contextJson.addProperty("page", context.getPage());
        }
        if (context.getTripPlanId() != null) {
            contextJson.addProperty("tripPlanId", context.getTripPlanId());
        }
        return contextJson;
    }

    private List<ChatbotRequest.HistoryItemDTO> sanitizeHistory(List<ChatbotRequest.HistoryItemDTO> history) {
        if (history == null || history.isEmpty()) {
            return List.of();
        }

        return history.stream()
                .filter(item -> item != null)
                .filter(item -> item.getRole() != null && !item.getRole().isBlank())
                .filter(item -> item.getContent() != null && !item.getContent().isBlank())
                .toList();
    }

    private String buildRecentConversation(List<ChatbotRequest.HistoryItemDTO> history) {
        if (history == null || history.isEmpty()) {
            return "No previous conversation.";
        }

        int startIndex = Math.max(0, history.size() - MAX_RECENT_HISTORY_ITEMS);
        List<ChatbotRequest.HistoryItemDTO> recentHistory = history.subList(startIndex, history.size());
        StringBuilder transcript = new StringBuilder();

        for (ChatbotRequest.HistoryItemDTO item : recentHistory) {
            if (transcript.length() > 0) {
                transcript.append('\n');
            }
            transcript.append(resolveSpeaker(item.getRole()))
                    .append(": ")
                    .append(item.getContent().trim());
        }

        return transcript.toString();
    }

    private String resolveSpeaker(String role) {
        if (role == null) {
            return "Unknown";
        }

        String normalizedRole = role.trim().toLowerCase();
        if (normalizedRole.equals("user")) {
            return "User";
        }
        if (normalizedRole.equals("assistant") || normalizedRole.equals("bot")) {
            return "Assistant";
        }
        return role.trim();
    }

    private ChatbotService.Interpretation parseInterpretation(String rawText) {
        JsonObject parsed = parseJsonObject(rawText, "LLM 질문 해석");
        ChatbotService.Mode mode = parseMode(readText(parsed.get("mode")));
        ChatbotService.ResolvedContext resolvedContext = parseResolvedContext(parsed.get("resolvedContext"));
        List<ChatbotService.QueryPlan> queryPlans = parseQueryPlans(parsed.get("queryPlans"));

        if (mode == ChatbotService.Mode.DB_QA && queryPlans.isEmpty()) {
            return ChatbotService.Interpretation.createGeneralChatInterpretation();
        }

        return ChatbotService.Interpretation.createInterpretation(mode, queryPlans, resolvedContext);
    }

    private ChatbotService.ResolvedContext parseResolvedContext(JsonElement node) {
        if (node == null || !node.isJsonObject()) {
            return ChatbotService.ResolvedContext.createEmptyContext();
        }

        JsonObject resolvedContext = node.getAsJsonObject();
        return ChatbotService.ResolvedContext.createResolvedContext(
                parseDomain(readText(resolvedContext.get("domain"))),
                readText(resolvedContext.get("intent")),
                readText(resolvedContext.get("region")),
                readText(resolvedContext.get("targetDate")),
                readText(resolvedContext.get("endDate")),
                readText(resolvedContext.get("keyword")),
                readText(resolvedContext.get("category")),
                readInteger(resolvedContext.get("tripPlanId"), null),
                readInteger(resolvedContext.get("limit"), null),
                readBoolean(resolvedContext.get("isFollowUp")),
                readStringList(resolvedContext.get("missingFields")));
    }

    private List<ChatbotService.QueryPlan> parseQueryPlans(JsonElement node) {
        List<ChatbotService.QueryPlan> queryPlans = new ArrayList<>();
        if (node == null || !node.isJsonArray()) {
            return queryPlans;
        }

        JsonArray array = node.getAsJsonArray();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject queryPlan = element.getAsJsonObject();
            ChatbotService.Domain domain = parseDomain(readText(queryPlan.get("domain")));
            if (domain == null) {
                continue;
            }
            queryPlans.add(ChatbotService.QueryPlan.createQueryPlan(
                    domain,
                    readText(queryPlan.get("intent")),
                    readText(queryPlan.get("keyword")),
                    readText(queryPlan.get("startDate")),
                    readText(queryPlan.get("endDate")),
                    readText(queryPlan.get("category")),
                    readInteger(queryPlan.get("limit"), 5)));
            if (queryPlans.size() >= 3) {
                break;
            }
        }
        return queryPlans;
    }

    private ChatbotService.Mode parseMode(String value) {
        if (value == null || value.isBlank()) {
            return ChatbotService.Mode.GENERAL_CHAT;
        }
        try {
            return ChatbotService.Mode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ChatbotService.Mode.GENERAL_CHAT;
        }
    }

    private ChatbotService.Domain parseDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ChatbotService.Domain.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String callOpenAi(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "OPENAI_API_KEY가 설정되지 않았습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HttpURLConnection connection = null;
        try {
            String requestBody = GSON.toJson(buildRequestPayload(systemPrompt, userPrompt));
            byte[] bodyBytes = requestBody.getBytes(StandardCharsets.UTF_8);

            connection = openConnection();
            connection.setFixedLengthStreamingMode(bodyBytes.length);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(bodyBytes);
            }

            int statusCode = connection.getResponseCode();
            String responseBody = readResponseBody(connection, statusCode);

            if (statusCode >= 400) {
                throw new ApiException(
                        "CHATBOT_INTERNAL_ERROR",
                        "OpenAI API 호출에 실패했습니다. status=" + statusCode,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String answer = extractOutputText(responseBody);
            if (answer.isBlank()) {
                throw new ApiException(
                        "CHATBOT_INTERNAL_ERROR",
                        "OpenAI 응답 텍스트가 비어 있습니다.",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return answer;
        } catch (ApiException e) {
            throw e;
        } catch (IOException e) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "OpenAI 호출 중 I/O 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        } catch (RuntimeException e) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "OpenAI 응답 처리 중 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private HttpURLConnection openConnection() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(endpoint).toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout((int) CONNECT_TIMEOUT.toMillis());
        connection.setReadTimeout((int) REQUEST_TIMEOUT.toMillis());
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json");
        return connection;
    }

    private String readResponseBody(HttpURLConnection connection, int statusCode) throws IOException {
        InputStream inputStream = statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
        if (inputStream == null) {
            return "";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }
            return responseBody.toString();
        }
    }

    private JsonObject buildRequestPayload(String systemPrompt, String userPrompt) {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", model);

        JsonArray input = new JsonArray();
        input.add(buildInputMessage("system", systemPrompt));
        input.add(buildInputMessage("user", userPrompt));
        payload.add("input", input);

        return payload;
    }

    private JsonObject buildInputMessage(String role, String text) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);

        JsonObject content = new JsonObject();
        content.addProperty("type", "input_text");
        content.addProperty("text", text);

        JsonArray contentArray = new JsonArray();
        contentArray.add(content);
        message.add("content", contentArray);
        return message;
    }

    private String extractOutputText(String responseBody) {
        JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
        String outputText = readText(root.get("output_text"));
        if (!outputText.isBlank()) {
            return outputText.trim();
        }

        JsonElement outputElement = root.get("output");
        if (outputElement != null && outputElement.isJsonArray()) {
            JsonArray outputArray = outputElement.getAsJsonArray();
            for (JsonElement outputNode : outputArray) {
                if (!outputNode.isJsonObject()) {
                    continue;
                }
                JsonElement contentElement = outputNode.getAsJsonObject().get("content");
                if (contentElement == null || !contentElement.isJsonArray()) {
                    continue;
                }
                JsonArray contentArray = contentElement.getAsJsonArray();
                for (JsonElement contentNode : contentArray) {
                    if (!contentNode.isJsonObject()) {
                        continue;
                    }
                    String text = readText(contentNode.getAsJsonObject().get("text"));
                    if (!text.isBlank()) {
                        return text.trim();
                    }
                }
            }
        }

        return "";
    }

    private JsonObject parseJsonObject(String rawText, String sourceName) {
        String candidate = rawText == null ? "" : rawText.trim();
        if (candidate.startsWith("```")) {
            candidate = stripCodeFence(candidate);
        }
        int start = candidate.indexOf('{');
        int end = candidate.lastIndexOf('}');
        if (start >= 0 && end > start) {
            candidate = candidate.substring(start, end + 1);
        }
        JsonElement parsed = JsonParser.parseString(candidate);
        if (!parsed.isJsonObject()) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    sourceName + " 응답이 JSON 객체 형식이 아닙니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return parsed.getAsJsonObject();
    }

    private String stripCodeFence(String text) {
        String stripped = text.trim();
        if (stripped.startsWith("```json")) {
            stripped = stripped.substring(7).trim();
        } else if (stripped.startsWith("```")) {
            stripped = stripped.substring(3).trim();
        }
        if (stripped.endsWith("```")) {
            stripped = stripped.substring(0, stripped.length() - 3).trim();
        }
        return stripped;
    }

    private JsonElement toJsonElement(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return new JsonObject();
        }
        try {
            return JsonParser.parseString(rawJson);
        } catch (Exception ignored) {
            JsonObject fallback = new JsonObject();
            fallback.addProperty("raw", rawJson);
            return fallback;
        }
    }

    private String readText(JsonElement node) {
        if (node == null || node.isJsonNull() || !node.isJsonPrimitive() || !node.getAsJsonPrimitive().isString()) {
            return "";
        }
        return node.getAsString();
    }

    private Integer readInteger(JsonElement node, Integer defaultValue) {
        if (node == null || node.isJsonNull() || !node.isJsonPrimitive()) {
            return defaultValue;
        }
        try {
            return node.getAsInt();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean readBoolean(JsonElement node) {
        if (node == null || node.isJsonNull() || !node.isJsonPrimitive()) {
            return false;
        }
        try {
            return node.getAsBoolean();
        } catch (Exception e) {
            return false;
        }
    }

    private List<String> readStringList(JsonElement node) {
        if (node == null || !node.isJsonArray()) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        for (JsonElement element : node.getAsJsonArray()) {
            String value = readText(element);
            if (!value.isBlank()) {
                values.add(value.trim());
            }
        }
        return values;
    }
}
