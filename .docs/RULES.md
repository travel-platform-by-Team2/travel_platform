# RULES.md

본 문서는 이 프로젝트에서 사용하는 모든 Agent 작업의 최상위 통제 규칙이다.

## 1. 우선순위

1. 사용자의 현재 지시
2. 본 문서 (`.docs/RULES.md`)
   <<<<<<< HEAD
3. Java 코드 컨벤션 정본 (`.docs/specs/java-code-convention.md`)
4. 개별 Agent 문서
5. 스킬 인덱스 문서 (`.docs/skills/00-스킬인덱스/00-스킬인덱스.md`)
6. 개별 스킬 문서 (`.docs/skills/*`)
7. # 작업 중 생성/수정되는 산출물 문서
8. 개별 Agent 문서
9. 스킬 인덱스 문서 (`.docs/skills/00-스킬인덱스/00-스킬인덱스.md`)
10. 개별 스킬 문서 (`.docs/skills/*`)
11. 작업 중 생성/수정되는 산출물 문서
    > > > > > > > dev

충돌 시에는 상위 우선순위와 더 보수적인 규칙을 우선 적용한다.

## 2. 스킬 탐색 절차

Agent 문서에서 RULES 참조 지시를 받은 경우, 아래 순서를 강제한다.

1. `.docs/RULES.md`를 먼저 확인한다.
   <<<<<<< HEAD
2. Java 코드 생성/수정 작업이면 `.docs/specs/java-code-convention.md`를 확인한다.
3. `.docs/skills/00-스킬인덱스/00-스킬인덱스.md`를 열어 요청 키워드/의도에 맞는 스킬 번호를 찾는다.
4. 선택한 스킬의 문서 경로로 이동해 가이드를 확인한다.
5. 복수 스킬이 필요하면 `00-스킬인덱스`의 복수 스킬 적용 규칙 순서대로 수행한다.
6. # 결과 보고 시 적용한 스킬 번호와 문서 경로를 명시한다.
7. `.docs/skills/00-스킬인덱스/00-스킬인덱스.md`를 열어 요청 키워드/의도에 맞는 스킬 번호를 찾는다.
8. 선택한 스킬의 문서 경로로 이동해 가이드를 확인한다.
9. 복수 스킬이 필요하면 `00-스킬인덱스`의 복수 스킬 적용 규칙 순서대로 수행한다.
10. 결과 보고 시 적용한 스킬 번호와 문서 경로를 명시한다.
    > > > > > > > dev

## 3. 승인 필수 작업

아래 변경은 사용자 승인 없이 진행하지 않는다.

1. Entity 구조 변경
2. DB schema 변경
3. 인증/권한 로직 변경
4. 공개 API(URL/요청/응답) 스펙 변경
5. `application.properties` 변경
6. 대규모 변경(파일 5개 이상 또는 diff 200줄 이상)
7. 파괴적 명령(`reset --hard`, `clean -fd`, 대량 파일 삭제)

## 4. 절대 금지

1. Controller -> Repository 직접 호출
2. DTO와 Entity 역할 혼합
3. 테스트 없이 핵심 도메인 로직 변경
4. 사용자 확인 없는 대규모 리팩토링
5. 인코딩/스키마를 추정으로 단정하고 원문 텍스트 의미를 변경

## 5. TASK 기반 실행

1. 작업은 TASK 단위로 수행한다.
2. TASK 범위를 벗어나는 변경이 필요하면 현재 작업을 중단하고 새 TASK로 분리한다.
3. HIGH 위험 변경은 승인 후 진행한다.
4. 스킬 적용이 필요한 TASK는 2번 섹션 절차를 따른다.

## 6. 작업 체크리스트

### 작업 전

1. 범위 확정
2. 위험도(LOW/MEDIUM/HIGH) 산정
3. 승인 필요 여부 확인
4. `00-스킬인덱스`에서 적용 스킬 확정
   <<<<<<< HEAD
5. 컨트롤러 타입 기준(`@Controller`/`@RestController`) 확정
6. # 도메인별 컨트롤러 분리 전략(`{Domain}Controller`/`{Domain}ApiController`) 확정
   > > > > > > > dev

### 작업 중

1. 범위 외 변경 금지
2. 중간 검증 수행
3. 구조 규칙 위반 여부 점검

### 작업 후

1. 변경 파일 목록 보고
2. 검증 결과(명령/결과) 보고
3. TODO/리스크 보고
4. 적용 스킬 번호/문서 경로 보고

## 7. 결과 보고 형식

1. 작업 요약
2. 변경 파일
3. 검증 결과
4. 미완료 항목(TODO)
5. 리스크/주의사항
6. 적용 스킬(번호/문서 경로)

## 8. 프로젝트 기본 원칙

1. 테이블 생성은 Entity + JPA 설정으로 자동 생성한다.
2. `data.sql`은 더미 데이터(insert) 중심으로 관리한다.
3. 계층 구조는 Controller -> Service -> Repository를 유지한다.
4. JSON 데이터 송수신 목적의 컨트롤러는 클래스 레벨 `@RequestMapping` 경로에 `/api`를 포함하고 `@RestController`를 사용한다.
5. JSON 데이터 송수신 목적이 아닌 화면(View) 렌더링 컨트롤러는 `@Controller`를 사용한다.
6. View 반환과 JSON 응답은 같은 컨트롤러에서 혼용하지 않는다.
7. 동일 도메인에서 View와 JSON이 모두 필요하면 컨트롤러 클래스를 분리한다(예: `BookingController`, `BookingApiController`).
8. `@RequestBody`는 JSON 요청 본문을 받는 경우에만 사용한다.
9. JSON 응답은 `@RestController` 또는 `@ResponseBody`/`ResponseEntity`로 처리한다.
10. `GET` 메서드에서는 `@RequestBody`를 사용하지 않고 `@RequestParam`/`@PathVariable`을 사용한다.
11. `@RestController` 메서드에서는 뷰 이름(템플릿 경로 문자열)을 반환하지 않고 데이터만 반환한다.
12. Java 상세 코딩 규칙은 `.docs/specs/java-code-convention.md`를 정본으로 따른다.
