<!-- Parent: ../AI-CONTEXT.md -->

# validation

## 목적

공통 입력 검증 어노테이션과 validator를 모아두는 패키지다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `EnumCode.java` | 코드 기반 enum이 구현하는 공통 인터페이스 |
| `ValidEnumCode.java` | enum 코드 검증용 커스텀 어노테이션 |
| `EnumCodeValidator.java` | `ValidEnumCode`를 실제로 검사하는 validator |

## AI 작업 지침

- 도메인 request DTO에서 같은 코드 목록을 `@Pattern`으로 중복 하드코딩하지 말고 이 패키지의 validator를 우선 검토한다.
- validator는 framework 공통 규칙만 다루고, 도메인 권한/상태 검증 같은 비즈니스 규칙은 service로 넘긴다.
- enum 검증을 추가할 때는 enum 쪽에 `EnumCode` 구현과 `getCode()` 기준을 먼저 맞춘다.
- validator에서 사용자 입력 오류를 판단하는 범위만 다루고, 응답 포맷 결정은 `_core/handler`에 맡긴다.

## 테스트

- `TripRequestValidationTest`
- `BoardRequestValidationTest`
- 관련 enum 코드 테스트와 컨트롤러 validation 테스트를 함께 확인한다.

## 의존성

- 내부: `../handler`, `../../trip/TripRegion.java`, `../../trip/TripCompanionType.java`, `../../board/BoardCategory.java`
- 외부: `Jakarta Validation`
