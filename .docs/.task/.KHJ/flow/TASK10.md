# TASK10 - Chatbot OpenAI LLM Integration

## 0. Work Type

- Feature implementation

## 1. Goal

- Replace the hardcoded `DIRECT_LLM` response with a real OpenAI API call.
- Keep `DB_QUERY` behavior unchanged.

## 2. Scope

- Add OpenAI client component for chatbot
- Connect `ChatbotService` `DIRECT_LLM` path to the OpenAI client
- Update chatbot service tests
- Sync task/progress/worklog docs

## 3. Out of Scope

- DB schema/entity changes
- Auth/session/filter policy changes
- `application.properties` changes
- SQL security policy changes
- Validation policy changes

## 4. Done Criteria

- [x] `needsDb=false` requests use OpenAI answer text in response.
- [x] OpenAI call failure is handled as `CHATBOT_INTERNAL_ERROR`.
- [x] `./gradlew test --tests "*ChatbotServiceTest"` passes.

## 5. Impact

- Layer: chatbot service/llm/test
- Feature: `DIRECT_LLM` answer generation
- User impact: general chatbot answers are now LLM-generated

## 6. Risk

- Level: MEDIUM
- Reason: external API/network/environment variable dependency added

## 7. Approval

- Required: No
- Reason: no `application.properties` edit; env var based config only

## 8. Workflow

### 8.1 Steps

| Step | Goal | Input | Output | Verification |
|---|---|---|---|---|
| 1 | Define integration boundary | current `ChatbotService` and tests | LLM client interface design | design review |
| 2 | Implement OpenAI client | OpenAI API spec | API call + response parsing code | `./gradlew compileJava` |
| 3 | Wire service and tests | step 1/2 output | service wiring + updated tests | chatbot tests |
| 4 | Sync docs | changed files + test results | updated task/progress/worklog | doc review |

### 8.2 Stop Conditions

- Public API contract change is required
- `application.properties` edit becomes mandatory
- Session/auth dependency becomes mandatory for this task

## 9. Verification Plan

- Build: `./gradlew compileJava`
- Tests:
  - `./gradlew test --tests "*ChatbotServiceTest"`
  - `./gradlew test --tests "*ChatbotControllerTest"`
  - `./gradlew test`
- Manual check:
  - verify `DIRECT_LLM` error path when `OPENAI_API_KEY` is not set

## 10. Result

- Changed files:
  - `build.gradle`
  - `src/main/java/com/example/travel_platform/chatbot/ChatbotService.java`
  - `src/main/java/com/example/travel_platform/chatbot/llm/ChatbotLlmClient.java`
  - `src/main/java/com/example/travel_platform/chatbot/llm/OpenAiChatbotLlmClient.java`
  - `src/test/java/com/example/travel_platform/chatbot/ChatbotServiceTest.java`
  - `.docs/.task/.KHJ/flow/TASK10.md`
  - `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
  - `.docs/.task/.KHJ/WORKLOG.md`
- Test results:
  - `./gradlew compileJava` success
  - `./gradlew test --tests "*ChatbotServiceTest"` success
  - `./gradlew test --tests "*ChatbotControllerTest"` success
  - `./gradlew test` success
- Remaining TODO:
  - apply SQL security policy
  - define auth/filter policy for chatbot API
- Risks/Notes:
  - Runtime needs `OPENAI_API_KEY`; missing key returns `CHATBOT_INTERNAL_ERROR` on `DIRECT_LLM` path
  - `OpenAiChatbotLlmClient` currently uses Responses API endpoint `/v1/responses`
