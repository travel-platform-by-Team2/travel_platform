<!-- Parent: ../AI-CONTEXT.md -->

# seller

## 목적

판매자 센터 화면과 향후 판매자 전용 API/도메인 확장을 위한 시작 패키지다.

## 주요 파일

| 파일명 | 설명 |
| --- | --- |
| SellerController.java | 판매자 센터 SSR 페이지(`/seller...`)를 반환한다. |
| SellerApiController.java | 판매자 센터 API 뼈대를 `/api/seller...` 아래에 둔다. |
| SellerService.java | 대시보드/상품/예약/정산 예시 응답을 제공하는 서비스 스켈레톤이다. |
| SellerRequest.java | 판매자 센터 입력 DTO 뼈대를 정의한다. |
| SellerResponse.java | 판매자 센터 응답 DTO 뼈대를 정의한다. |
| SellerProduct.java | 판매자 상품 엔티티 뼈대다. |
| SellerBooking.java | 판매자 예약 엔티티 뼈대다. |
| SellerSettlement.java | 판매자 정산 엔티티 뼈대다. |

## AI 작업 지침

- 화면 라우트는 `@RequestMapping("/seller")` 아래에서 `/seller`, `/seller/products`, `/seller/bookings`, `/seller/settlements`를 유지한다.
- API 라우트는 프로젝트 규칙에 맞춰 `/api/seller/...`를 사용한다.
- 현재는 권한 체크가 없으므로, 판매자 영역이라는 전제만 유지하고 인증/role 로직은 섣불리 추가하지 않는다.
- 공통 헤더/푸터는 기존 partial을 재사용하고, 판매자 전용 왼쪽 패널은 `partials/seller-sidebar.mustache`와 `seller.css`로 관리한다.
- 왼쪽 패널은 페이지별 콘텐츠 높이와 무관하게 하단이 잘리지 않도록 sticky + overflow 구조를 유지한다.

## 테스트

- `./gradlew.bat compileJava`

## 참조

- 템플릿: `src/main/resources/templates/pages/seller-*`
- 스타일: `src/main/resources/static/css/seller.css`
