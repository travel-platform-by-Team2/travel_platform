# KHJ 작업 로그

## 개요
- 작성자: KHJ
- 작성일: 2026-03-03
- 목적: 팀 개발 진행 상황 기록 및 다음 작업 인계

## 오늘 완료한 작업
1. 도메인 엔티티 생성
   - `TripPlan`, `TripPlace`
   - `CommunityPost`, `CommunityReply`
   - `Booking`
   - `CalendarEvent`

2. 도메인별 계층 골격 생성 (로직은 TODO 중심)
   - `trip`, `community`, `booking`, `calendar`
   - `Repository / Service / Controller` 클래스 생성

3. 도메인별 DTO 생성
   - `Request`, `Response` 클래스 생성
   - 하위 DTO(static class)까지 역할 주석 추가

4. Lombok 어노테이션 정리
   - 엔티티의 `@Getter`, `@Setter` 조합을 `@Data`로 통일

5. 데이터 초기화 정책 정리
   - `data.sql`은 **더미 데이터 전용**으로 정리
   - `create table` 구문 제거 완료
   - FK 참조값은 더미 목적에 맞게 **하드코딩 ID 방식**으로 작성

6. 검증
   - `./gradlew test` 실행 및 통과

## 현재 상태
- 테이블 생성: 엔티티 + `ddl-auto=create`로 자동 생성
- `data.sql`: 샘플 데이터 insert만 유지
- 서비스/컨트롤러 내부 구현: 대부분 `TODO` 상태 (팀원 분담 작업 예정)

## 다음 작업
1. 세션 사용자 임시값(`1`)을 실제 세션 컨텍스트로 교체
2. Service 레이어 DTO <-> Entity 매핑 구현
3. Repository 조회/수정 쿼리 구현
4. 권한/검증 로직 구현
5. 도메인별 통합 테스트 추가

## 참고
- 현재 구조는 팀 병렬 개발을 위한 스켈레톤 우선 구성
- 실제 비즈니스 로직 구현 전, API 스펙 확정 필요
