# TASK16 - OpenAI 키 `.env` 외부화

## 0. 작업 유형

- 설정 정리

## 1. 작업 목표

- OpenAI API 키를 코드/설정 파일 하드코딩 없이 `.env` 파일로 분리한다.
- Spring Boot 실행 시 `.env` 값을 자동으로 읽도록 연결한다.

## 2. 작업 범위

- `src/main/resources/application.properties` 설정 추가
- `.gitignore`에 `.env` 제외 규칙 추가
- `.env.example` 샘플 파일 추가
- 컴파일 및 챗봇 테스트 확인

## 3. 작업 제외 범위

- 챗봇 비즈니스 로직 변경
- API 스펙 변경
- DB schema 변경

## 4. 완료 기준

- [x] `.env` 파일이 Git 추적 대상에서 제외된다.
- [x] Spring이 `.env`를 읽어 `OPENAI_API_KEY`를 주입할 수 있다.
- [x] `./gradlew compileJava` 성공
- [x] `./gradlew test --tests "*ChatbotServiceTest" --tests "*ChatbotControllerTest"` 성공

## 5. 위험도

- LOW (설정 연결 작업)

## 6. 결과 기록

- 변경 파일:
  - `src/main/resources/application.properties`
  - `.gitignore`
  - `.env.example`
  - `.docs/.task/.KHJ/flow/TASK16.md`
  - `.docs/.task/.KHJ/CHATBOT_PROGRESS.md`
  - `.docs/.task/.KHJ/WORKLOG.md`
- 로컬 생성(비추적): `.env`
- 런타임 확인:
  - `bootRun` 후 `POST /api/chatbot/messages` 호출 성공 확인
