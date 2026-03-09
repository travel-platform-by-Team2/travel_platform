# TASK11 - OpenAI client JSON parser migration to Gson

## 0. Work Type

- Refactoring

## 1. Goal

- Replace Jackson-based JSON handling in OpenAI chatbot client with Gson.
- Keep chatbot behavior and API contract unchanged.

## 2. Scope

- Update `OpenAiChatbotLlmClient` imports/serialization/parsing logic
- Verify compile and chatbot tests
- Sync worklog

## 3. Out of Scope

- `build.gradle` dependency policy change (already user-managed)
- Chatbot controller/service contract changes
- DB query flow changes

## 4. Done Criteria

- [x] `OpenAiChatbotLlmClient` no longer uses `ObjectMapper`/`JsonNode`.
- [x] Gson-based payload/response handling works for `output_text` and fallback `output[].content[].text`.
- [x] `./gradlew test --tests "*ChatbotServiceTest"` passes.

## 5. Risk

- Level: LOW
- Reason: parser library swap in a single component

## 6. Verification

- `./gradlew compileJava`
- `./gradlew test --tests "*ChatbotServiceTest"`
- `./gradlew test`

## 7. Result

- Changed files:
  - `src/main/java/com/example/travel_platform/chatbot/llm/OpenAiChatbotLlmClient.java`
  - `.docs/.task/.KHJ/flow/TASK11.md`
  - `.docs/.task/.KHJ/WORKLOG.md`
- Test results:
  - `./gradlew compileJava` success
  - `./gradlew test --tests "*ChatbotServiceTest"` success
  - `./gradlew test` success
- Notes:
  - `build.gradle` was not modified in this task (kept user-managed state)
