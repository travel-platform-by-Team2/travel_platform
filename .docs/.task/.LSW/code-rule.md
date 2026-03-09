# Code Rule

`.docs/RULES.md` 기준으로 실무 적용 시 필요한 규칙만 정리한 요약본이다.

## 1) 규칙 우선순위
1. 사용자의 현재 지시
2. `.docs/RULES.md`
3. `.docs/specs/java-code-convention.md`
4. 개별 Agent 문서
5. 스킬 인덱스 문서 (`.docs/skills/00-스킬인덱스/00-스킬인덱스.md`)
6. 개별 스킬 문서 (`.docs/skills/*`)
7. 작업 중 생성/수정되는 산출물 문서

충돌 시 상위 우선순위 + 더 보수적인 규칙을 적용한다.

## 2) 스킬 적용 절차
1. `.docs/RULES.md` 확인
2. Java 코드 생성/수정이면 `.docs/specs/java-code-convention.md` 확인
3. 스킬 인덱스에서 요청 의도에 맞는 스킬 선택
4. 선택한 스킬 가이드 확인 후 수행
5. 복수 스킬 필요 시 인덱스의 적용 순서 준수
6. 결과 보고에 적용 스킬 번호/문서 경로 명시

## 3) 승인 필수 작업
아래는 사용자 승인 없이 진행하지 않는다.
1. Entity 구조 변경
2. DB schema 변경
3. 인증/권한 로직 변경
4. 공개 API(URL/요청/응답) 스펙 변경
5. `application.properties` 변경
6. 대규모 변경(파일 5개 이상 또는 diff 200줄 이상)
7. 파괴적 명령(`reset --hard`, `clean -fd`, 대량 파일 삭제)

## 4) 절대 금지
1. `Controller -> Repository` 직접 호출
2. DTO/Entity 역할 혼합
3. 테스트 없이 핵심 도메인 로직 변경
4. 사용자 확인 없는 대규모 리팩토링
5. 인코딩/스키마를 추정으로 단정하고 원문 의미 변경

## 5) TASK 기반 실행
1. 작업은 TASK 단위로 수행
2. TASK 범위 밖 변경 필요 시 현재 작업 중단 후 새 TASK 분리
3. HIGH 위험 변경은 승인 후 진행
4. 스킬 적용 필요 TASK는 스킬 적용 절차 준수

## 6) 작업 체크리스트
### 작업 전
1. 범위 확정
2. 위험도(LOW/MEDIUM/HIGH) 산정
3. 승인 필요 여부 확인
4. 적용 스킬 확정
5. 컨트롤러 타입 확정(`@Controller`/`@RestController`)
6. 도메인별 컨트롤러 분리 전략 확정 (`{Domain}Controller`/`{Domain}ApiController`)

### 작업 중
1. 범위 외 변경 금지
2. 중간 검증 수행
3. 구조 규칙 위반 점검

### 작업 후
1. 변경 파일 목록 보고
2. 검증 결과(명령/결과) 보고
3. TODO/리스크 보고
4. 적용 스킬 번호/문서 경로 보고

## 7) 결과 보고 형식
1. 작업 요약
2. 변경 파일
3. 검증 결과
4. 미완료 항목(TODO)
5. 리스크/주의사항
6. 적용 스킬(번호/문서 경로)

## 8) 프로젝트 기본 원칙
1. 테이블 생성은 Entity + JPA 설정으로 자동 생성
2. `data.sql`은 더미 데이터(insert) 중심 관리
3. 계층 구조는 `Controller -> Service -> Repository` 유지
4. JSON API 컨트롤러는 클래스 레벨 경로에 `/api` 포함 + `@RestController` 사용
5. View 렌더링 컨트롤러는 `@Controller` 사용
6. View 반환과 JSON 응답을 같은 컨트롤러에서 혼용 금지
7. 동일 도메인에서 View/JSON 동시 필요 시 컨트롤러 분리
8. `@RequestBody`는 JSON 본문 수신 시에만 사용
9. JSON 응답은 `@RestController` 또는 `@ResponseBody`/`ResponseEntity`로 처리
10. `GET` 메서드에서 `@RequestBody` 사용 금지 (`@RequestParam`/`@PathVariable` 사용)
11. `@RestController`에서 뷰 이름 문자열 반환 금지
12. Java 상세 코딩 규칙은 `.docs/specs/java-code-convention.md`를 정본으로 따른다
