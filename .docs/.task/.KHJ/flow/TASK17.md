# TASK17 - 프로젝트 ERD 다이어그램 문서화

## 0. 작업 유형

- 문서화

## 1. 작업 목표

- 현재 프로젝트 기준으로 테이블 관계를 한눈에 볼 수 있는 ERD를 스펙 문서에 반영한다.

## 2. 작업 범위

- `.docs/specs/table-specification.md`에 ERD(mermaid) 추가
- 기존 관계 요약 표와 ERD 내용 정합성 확인

## 3. 작업 제외 범위

- 엔티티/테이블 구조 변경
- 코드 로직 변경

## 4. 완료 기준

- [x] 스펙 문서에서 렌더 가능한 ERD 다이어그램이 제공된다.
- [x] JPA 기반 테이블 + SQL 전용 보조 테이블 관계가 반영된다.

## 5. 위험도

- LOW (문서 반영만 수행)

## 6. 결과 기록

- 변경 파일:
  - `.docs/specs/table-specification.md`
  - `.docs/.task/.KHJ/flow/TASK17.md`
- 비고:
  - `map_place_image_tb`는 독립 테이블로 ERD 하단 설명에 별도 명시
