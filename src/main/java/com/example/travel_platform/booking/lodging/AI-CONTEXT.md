<!-- Parent: ../AI-CONTEXT.md -->

# booking/lodging

## 목적

`booking` 도메인에서 숙소 POI 조회와 숙소 보조 데이터 저장을 담당한다.

## 파일

- `Lodging.java`
- `LodgingMinPrice.java`
- `LodgingQueryRepository.java`

## 구조 메모

- `booking` 루트의 대표 흐름과 분리된 보조 엔티티라서 하위 폴더로 이동했다.
- `LodgingQueryRepository`는 지도 bounds + `regionKey` 기준 JPQL 조회를 담당한다.
- 예약 생성/완료/목록/상세/취소 흐름은 계속 `booking/` 루트의 `BookingService`가 담당한다.
