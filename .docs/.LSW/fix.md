# map-detail H2 전환 수정 내역

작성일: 2026-03-06
대상 브랜치/워크스페이스: `c:\workspace\travel_platform`
요청 요약: `map-detail.mustache`에서 사용하는 DB 데이터를 MySQL 기준이 아닌 H2 기준으로 동작하도록 `data.sql`, `mapdata.sql` 수정

## 1) 수정 배경

프로젝트는 현재 `application.properties` 기준으로 H2 메모리 DB(`jdbc:h2:mem:test`)를 사용합니다.
하지만 map-detail 관련 데이터는 `db/mapdata.sql`에 분리되어 있었고, 기본 SQL 초기화 경로(`spring.sql.init.data-locations`)는 `db/data.sql`만 바라보고 있어 실제 초기화 시 map 관련 시드가 누락될 여지가 있었습니다.

또한 기존 SQL에는 다음 리스크가 있었습니다.
- `now()` 등 DB별 함수 사용(호환성 이슈 가능)
- 일부 문자열/따옴표 깨짐으로 SQL 파싱 실패 가능
- 초기 저장 시 UTF-8 BOM 이 포함되면 H2가 첫 줄에서 문법 오류 발생 가능

## 2) 수정 파일

- `src/main/resources/db/data.sql`
- `src/main/resources/db/mapdata.sql`

## 3) 파일별 상세 수정

### A. `src/main/resources/db/data.sql`

#### A-1. 시간 함수 통일 (H2 안정화)
- 변경 전: `now()`
- 변경 후: `current_timestamp`
- 적용 범위: `user_tb`, `trip_plan_tb`, `board_tb`, `board_reply_tb`, `booking_tb` 등 created_at 컬럼

#### A-2. 시드 문장 정리
- 기존 데이터의 문장/표현 일부를 정리하여 한글 텍스트 가독성 및 샘플 일관성 개선
- 날짜/타임스탬프 리터럴은 H2 표준 형식(`date 'YYYY-MM-DD'`, `timestamp 'YYYY-MM-DD HH:MM:SS'`) 유지

#### A-3. mapdata.sql 실행 연결 (핵심)
- 파일 하단에 아래 구문 추가:

```sql
runscript from 'classpath:db/mapdata.sql';
```

의미:
- Spring SQL 초기화가 `data.sql`만 실행하더라도, map 관련 테이블/시드(`map_place_image_tb`, `lodging_tb`)가 함께 초기화됨
- 결과적으로 `map-detail`에서 호출하는 저장소 쿼리의 기반 데이터가 보장됨

### B. `src/main/resources/db/mapdata.sql`

#### B-1. map 캐시 테이블 생성문 유지/정리
- `map_place_image_tb` 생성문을 H2 문법 기준으로 유지
- `merge into ... key (...)` 구문으로 upsert 방식 유지

#### B-2. map 이미지 시드 데이터 정리
- 깨진/불안정한 문자열 구간을 정리하여 SQL 파싱 안정화
- `normalized_name` 기준으로 중복 없이 upsert 되도록 구성

#### B-3. lodging 테이블 시드 강화
- `lodging_tb` 생성문 유지
- 성능/조회 보조를 위해 인덱스 추가:
  - `idx_lodging_region_active(region_key, is_active)`
  - `idx_lodging_geo(lat, lng)`
  - `idx_lodging_name(normalized_name)`

#### B-4. lodging 데이터 삽입 방식 변경
- 다건 `merge into lodging_tb ... key (external_place_id) values (...)` 추가
- 지역별 샘플 숙소 데이터 추가:
  - `busan`, `seoul`, `jeju`, `gyeongju`, `gangwon`

의미:
- `LodgingQueryRepository.findActiveLodgingsInBounds(...)` 조회 시 지도 범위/지역 필터에서 DB POI가 실제로 반환됨
- `map-detail`의 병합 API(`/api/bookings/map-pois/merge`)가 카카오 결과 + DB 결과를 안정적으로 조합 가능

## 4) 실행 검증

실행 명령:

```powershell
$env:GRADLE_USER_HOME='c:/workspace/travel_platform/.gradle-home'; ./gradlew test -q
```

검증 결과:
- 최종 테스트 통과 (실패 0)

중간 이슈 및 조치:
- 최초 재실행 시 `data.sql` 첫 줄에서 H2 문법 오류 발생
- 원인: UTF-8 BOM(`\uFEFF`) 포함
- 조치: `data.sql`, `mapdata.sql`를 UTF-8 BOM 없는 인코딩으로 재저장

## 5) map-detail 동작 관점에서의 영향

- `MapPlaceImageRepository`
  - `map_place_image_tb` 데이터 조회/업서트 정상 동작 기반 확보
- `LodgingQueryRepository`
  - `lodging_tb`의 활성 숙소/영역 조회 데이터 확보
- `map-detail.js`
  - `/api/bookings/map-pois/merge` 응답에서 DB 숙소가 병합되어 카드/오버레이 렌더링 가능

## 6) 남은 참고사항

- 현재 SQL 파일 내용은 저장 인코딩/터미널 코드페이지에 따라 콘솔 출력 시 한글이 깨져 보일 수 있음
- 실제 파일 인코딩은 UTF-8(BOM 없음)으로 맞춰 두었고, H2 초기화/테스트 기준으로는 정상 동작 확인

## 7) 최종 변경 요약

1. `data.sql`의 시간 함수/시드 문구 정리
2. `data.sql`에서 `mapdata.sql`을 `runscript`로 연결
3. `mapdata.sql`의 map 이미지 및 숙소 시드 구조 H2 기준으로 정리
4. `lodging_tb` 인덱스 및 지역 샘플 데이터 추가
5. BOM 제거로 H2 초기화 문법 오류 해결
