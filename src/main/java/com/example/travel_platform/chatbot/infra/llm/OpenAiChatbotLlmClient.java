package com.example.travel_platform.chatbot.infra.llm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.travel_platform._core.handler.ex.ApiException;
import com.example.travel_platform.chatbot.api.dto.ChatbotRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * OpenAI Responses API 기반 챗봇 LLM 클라이언트 구현체.
 *
 * 역할:
 * 1) 사용자 질문에 대한 1차 계획(JSON) 생성
 * 2) DB 조회 결과를 바탕으로 2차 최종 답변(JSON) 생성
 */
@Component
public class OpenAiChatbotLlmClient implements ChatbotLlmClient {

    /** OpenAI 연결 타임아웃 */
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /** OpenAI 요청 전체 타임아웃 */
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    /**
     * 1차 계획 생성을 위한 시스템 프롬프트.
     * 스토리보드 기준으로 DB 필요 여부, SQL, direct answer를 구조화한다.
     */
    private static final String PLAN_SYSTEM_PROMPT = """
            너는 여행 플랫폼 챗봇의 질의 계획기다.
            반드시 아래 JSON 스키마만 반환해라.
            {
              "needsDb": boolean,
              "queryIntent": string,
              "querySummary": string,
              "sql": string,
              "answer": string
            }
            규칙:
            - DB 조회가 필요하면 needsDb=true로 설정하고 읽기 전용 SELECT SQL을 작성해라.
            - DB 조회가 필요 없으면 needsDb=false로 설정하고 answer에 최종 답변을 작성해라.
            - markdown, 코드블록(```), 설명 문장은 넣지 마라.
            - 테이블/컬럼 참조는 반드시 schemaContext에 제공된 정보만 사용해라.
            - 목록 조회 시 사용자가 개수를 명시하지 않으면 LIMIT 5를 사용해라.
            """;

    /** DB 조회가 필요 없는 질문의 답변 생성 프롬프트 */
    private static final String GENERAL_ANSWER_SYSTEM_PROMPT = """
            너는 여행 플랫폼 챗봇이다.
            사용자 질문에 대해 간결하고 정확한 한국어 답변을 작성해라.
            """;

    /**
     * DB 조회 결과 기반 2차 답변 프롬프트.
     * 최종 응답 형식은 JSON(answer)으로 강제한다.
     */
    private static final String DB_ANSWER_SYSTEM_PROMPT = """
            너는 여행 플랫폼 챗봇이다.
            userMessage와 rows를 사용해 최종 한국어 답변을 생성해라.
            rows가 비어 있으면 데이터가 없다는 사실을 명확히 안내해라.
            답변은 직관적인 문장으로 작성해라.
            반드시 아래 JSON 스키마만 반환해라.
            {
              "answer": string
            }
            markdown, 코드블록(```), 설명 문장은 넣지 마라.
            """;

    /** Gson 인스턴스(요청/응답 JSON 직렬화 및 역직렬화) */
    private static final Gson GSON = new Gson();

    /** queryIntent 기본값 */
    private static final String FALLBACK_INTENT = "GENERAL_CHAT";

    /** querySummary 기본값 */
    private static final String FALLBACK_QUERY_SUMMARY = "LLM 생성 SQL 조회";

    /** OpenAI API Key */
    private final String apiKey;

    /** OpenAI 모델명 */
    private final String model;

    /** OpenAI Responses API 엔드포인트 */
    private final String endpoint;

    public OpenAiChatbotLlmClient(
            @Value("${OPENAI_API_KEY:}") String apiKey,
            @Value("${OPENAI_MODEL:gpt-4.1-mini}") String model,
            @Value("${OPENAI_ENDPOINT:https://api.openai.com/v1/responses}") String endpoint) {
        this.apiKey = apiKey;
        this.model = model;
        this.endpoint = endpoint;
    }

    /**
     * 사용자 질문을 기반으로 1차 실행 계획을 생성한다.
     */
    @Override
    public ChatbotLlmPlan createPlan(String userMessage, ChatbotRequest.ContextDTO context, String schemaContext) {
        try {
            String raw = callOpenAi(
                    PLAN_SYSTEM_PROMPT,
                    buildPlanUserPrompt(userMessage, context, schemaContext));

            JsonObject plan = parseJsonObject(raw, "LLM 계획");
            boolean needsDb = readBoolean(plan.get("needsDb"), false);

            String queryIntent = readText(plan.get("queryIntent"));
            if (queryIntent.isBlank()) {
                queryIntent = FALLBACK_INTENT;
            }

            String querySummary = readText(plan.get("querySummary"));
            if (querySummary.isBlank()) {
                querySummary = FALLBACK_QUERY_SUMMARY;
            }

            String sql = readText(plan.get("sql"));
            String answer = readText(plan.get("answer"));

            // DB 분기인데 SQL이 비어 있으면 실행 자체가 불가능하므로 즉시 오류 처리
            if (needsDb && sql.isBlank()) {
                throw new ApiException(
                        "CHATBOT_INTERNAL_ERROR",
                        "LLM 계획에 SQL이 포함되지 않았습니다.",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // 비DB 분기인데 answer가 비어 있으면 일반 답변 프롬프트로 재생성 시도
            if (!needsDb && answer.isBlank()) {
                answer = callOpenAi(GENERAL_ANSWER_SYSTEM_PROMPT, userMessage);
            }

            return new ChatbotLlmPlan(needsDb, queryIntent, querySummary, sql, answer);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "LLM 계획 생성 중 오류가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    /**
     * DB 조회 결과를 기반으로 2차 최종 답변을 생성한다.
     * 응답은 JSON(answer) 형식으로 강제하고 파싱한다.
     */
    @Override
    public String createDbAnswer(String userMessage, String queryIntent, List<Map<String, Object>> queryRows) {
        JsonObject payload = new JsonObject();
        payload.addProperty("userMessage", userMessage);
        payload.addProperty("queryIntent", queryIntent);
        payload.add("rows", GSON.toJsonTree(queryRows));

        String raw = callOpenAi(DB_ANSWER_SYSTEM_PROMPT, GSON.toJson(payload));
        JsonObject parsed = parseJsonObject(raw, "LLM DB 답변");
        String answer = readText(parsed.get("answer"));

        if (answer.isBlank()) {
            throw new ApiException(
                    "CHATBOT_INTERNAL_ERROR",
                    "LLM DB 답변 JSON에 answer 필드가 없습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return answer;
    }

    /**
     * OpenAI Responses API 공통 호출 메서드.
     * 프롬프트 입력, 응답 텍스트 추출, 오류 처리를 담당한다.
     */
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
                        "OpenAI API 호출 실패(status=" + statusCode + ")",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String answer = extractOutputText(responseBody);
            if (answer.isBlank()) {
                throw new ApiException(
                        "CHATBOT_INTERNAL_ERROR",
                        "OpenAI 응답에 텍스트가 없습니다.",
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

    /**
     * 1차 계획 생성용 user 프롬프트를 JSON 문자열로 구성한다.
     */
    private String buildPlanUserPrompt(String userMessage, ChatbotRequest.ContextDTO context, String schemaContext) {
        JsonObject contextJson = new JsonObject();
        if (context != null) {
            if (context.getPage() != null && !context.getPage().isBlank()) {
                contextJson.addProperty("page", context.getPage());
            }
            if (context.getTripPlanId() != null) {
                contextJson.addProperty("tripPlanId", context.getTripPlanId());
            }
        }

        JsonObject promptJson = new JsonObject();
        promptJson.addProperty("message", userMessage);
        promptJson.add("context", contextJson);
        promptJson.add("schemaContext", toJsonElement(schemaContext));
        return GSON.toJson(promptJson);
    }

    /**
     * raw JSON 문자열을 JsonElement로 변환한다.
     * 파싱 실패 시 원문을 담은 fallback 객체를 반환한다.
     */
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

    /**
     * Responses API 요청 본문을 구성한다.
     */
    private JsonObject buildRequestPayload(String systemPrompt, String userPrompt) {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", model);

        JsonArray input = new JsonArray();
        input.add(buildInputMessage("system", systemPrompt));
        input.add(buildInputMessage("user", userPrompt));
        payload.add("input", input);

        return payload;
    }

    /**
     * Responses API input message 1건을 구성한다.
     */
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

    /**
     * LLM 응답 텍스트에서 JSON 객체를 추출한다.
     * 코드블록으로 감싼 응답도 허용한다.
     */
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

    /**
     * markdown code fence를 제거한다.
     */
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

    /**
     * Responses API 응답에서 텍스트를 추출한다.
     * 우선순위: output_text -> output[].content[].text
     */
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

    /**
     * JsonElement를 boolean으로 안전하게 변환한다.
     */
    private boolean readBoolean(JsonElement node, boolean defaultValue) {
        if (node == null || node.isJsonNull() || !node.isJsonPrimitive()) {
            return defaultValue;
        }
        if (node.getAsJsonPrimitive().isBoolean()) {
            return node.getAsBoolean();
        }
        if (node.getAsJsonPrimitive().isString()) {
            return Boolean.parseBoolean(node.getAsString());
        }
        return defaultValue;
    }

    /**
     * JsonElement를 문자열로 안전하게 변환한다.
     */
    private String readText(JsonElement node) {
        if (node == null || node.isJsonNull() || !node.isJsonPrimitive() || !node.getAsJsonPrimitive().isString()) {
            return "";
        }
        return node.getAsString();
    }
}
