<!-- Parent: ../AI-CONTEXT.md -->

# booking/mapPlaceImage

## 목적

`booking` 도메인에서 장소 이미지 캐시 조회와 업서트 저장을 담당한다.

## 파일

- `MapPlaceImage.java`
- `MapPlaceImageRepository.java`
- `MapPlaceImageRepositoryCustom.java`
- `MapPlaceImageRepositoryImpl.java`

## 구조 메모

- `booking` 루트의 대표 예약 흐름과 분리된 보조 엔티티라서 하위 폴더로 이동했다.
- 캐시 조회는 `MapPlaceImageRepository`가 담당한다.
- 업서트는 `MapPlaceImageRepositoryImpl`이 H2/MySQL 분기까지 포함해 담당한다.
- 외부 이미지 조회 조합 흐름은 계속 `booking/` 루트의 `BookingService`가 담당한다.
