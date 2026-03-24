<!-- Parent: ../AI-CONTEXT.md -->

# weather

## 목적

챗봇과 여행계획 화면에서 공통으로 사용하는 날씨 API 도메인이다.
기상청 중기예보와 단기예보 Open API를 조합해서 특정 날짜 기준 최대 4일치 일별 예보를 제공한다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| `WeatherApiController.java` | 단일 날씨 API 진입점 |
| `WeatherService.java` | 특정 날짜부터 최대 4일 범위의 예보를 단기/중기예보에서 조합 |
| `WeatherRepository.java` | 기상청 중기예보/단기예보 Open API 호출 |
| `WeatherRegion.java` | 입력 지역과 regId, 단기예보 격자 좌표 매핑 |
| `WeatherResponse.java` | API 응답 DTO |

## 작업 메모

- 현재 API는 `GET /api/weather?region={region}&targetDate={yyyy-MM-dd}` 형식이다.
- 응답은 `targetDate`부터 최대 3일 뒤까지, 표시 가능한 날짜만 일별 목록으로 담는다.
- `targetDate`가 가까운 날짜면 단기예보, 4일 이후면 중기예보를 사용하고 범위가 걸치면 둘을 조합한다.
- `region`은 지역 코드(`jeju`, `seoul`)와 한글 지역명(`제주`, `서울`) 입력을 모두 허용한다.
- 외부 연동 실패는 `_core` 예외 체계로 전달한다.
