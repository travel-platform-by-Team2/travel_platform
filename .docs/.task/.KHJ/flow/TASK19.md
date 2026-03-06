# TASK19 - table-specification ERD 타입 표기 수정

## 0. 작업 유형

- 문서 수정

## 1. 작업 목표

- `table-specification.md`의 Mermaid ERD가 렌더링되도록 타입 표기를 Mermaid 친화형으로 수정한다.

## 2. 작업 범위

- `.docs/specs/table-specification.md`의 `4.2 ERD 다이어그램` 코드 블록만 수정

## 3. 작업 제외 범위

- 다이어그램 외 본문 변경
- 엔티티/테이블 구조 변경

## 4. 완료 기준

- [x] `NULL` 등 Mermaid 문법에 불리한 토큰 제거
- [x] 타입을 `int/string/float/date/datetime/boolean/text`로 정리
- [x] 관계선 및 섹션 구조 유지

## 5. 결과 기록

- 변경 파일:
  - `.docs/specs/table-specification.md`
  - `.docs/.task/.KHJ/flow/TASK19.md`
