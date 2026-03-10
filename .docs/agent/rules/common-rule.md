## **1. 목적**

이 프로젝트의 Java 소스 파일을 생성하거나 수정할 때 반드시 이 컨벤션을 따른다.

---

## **2. 적용 시점**

아래 요청 시 이 규칙을 우선 적용한다.

1. 엔티티 / 리포지토리 / 서비스 / 컨트롤러 / 요청 DTO / 응답 DTO 생성
2. 새 기능, 엔드포인트, 도메인 추가
3. 기존 코드의 컨벤션 위반 검토 및 수정

---

## **3. 패키지 구조**

도메인 기반 플랫 구조를 사용한다. 레이어 기반 구조는 사용하지 않는다.

```
com.example.travel_platform/
  _core/util/        <- 도메인 무관 공통 유틸 (Resp.java 등)
  {domain}/          <- 해당 도메인의 모든 파일을 한 폴더에 (플랫)
    {Domain}.java
    {Domain}Controller.java       <- SSR (Mustache)
    {Domain}ApiController.java    <- REST API (/api 접두사)
    {Domain}Service.java
    {Domain}Repository.java
    {Domain}Request.java
    {Domain}Response.java
```

---

## **4. 엔티티 규칙**

```java
@NoArgsConstructor
@Data
@Entity
@Table(name = "{domain}_tb")
public class {Domain} {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    private OtherEntity other;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public {Domain}(Integer id, ..., LocalDateTime createdAt) {
        this.id = id;
    }
}
```

필수 규칙:

1. `@Builder`는 생성자에만 선언한다. 클래스 레벨 선언 금지.
2. 모든 연관관계는 `FetchType.LAZY`만 사용한다.
3. PK 타입은 `Integer`만 사용한다.
4. 테이블명은 `{domain}_tb`를 사용한다.
5. 생성일 필드는 `@CreationTimestamp` + `LocalDateTime createdAt`를 사용한다.
6. 컬렉션 필드(`List`, `Set`)는 `@Builder` 생성자 인자에 포함하지 않는다.

---

## **5. 리포지토리 규칙**

```java
public interface {Domain}Repository extends JpaRepository<{Domain}, Integer> {
}
```

필수 규칙:

1. `JpaRepository<Entity, PK>`의 PK는 `Integer`를 사용한다.
2. 커스텀 메서드는 필요한 경우에만 추가한다.

---

## **6. 서비스 규칙**

```java
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class {Domain}Service {

    private final {Domain}Repository {domain}Repository;

    public {Domain}Response.Detail findById(Integer id) {
        {Domain} entity = {domain}Repository.findById(id).orElseThrow(...);
        return new {Domain}Response.Detail(entity);
    }

    @Transactional
    public void save({Domain}Request.Save req) {
        ...
    }
}
```

필수 규칙:

1. 클래스 레벨 `@Transactional(readOnly = true)`를 선언한다.
2. 쓰기 메서드(`save`, `update`, `delete`)에는 메서드 레벨 `@Transactional`을 선언한다.
3. DTO는 Service에서 생성해 반환한다.
4. Entity를 Controller로 전달하지 않는다.

---

## **7. 컨트롤러 규칙**

### **7.1 SSR 컨트롤러**

```java
@RequiredArgsConstructor
@Controller
public class {Domain}Controller {

    private final {Domain}Service {domain}Service;
    private final HttpSession session;

    @GetMapping("/{domain}s")
    public String index(Model model) {
        model.addAttribute("items", {domain}Service.findAll());
        return "{domain}/index";
    }
}
```

### **7.2 REST 컨트롤러**

```java
@RequiredArgsConstructor
@RestController
public class {Domain}ApiController {

    private final {Domain}Service {domain}Service;

    @GetMapping("/api/{domain}s/{id}")
    public ResponseEntity<?> detail(@PathVariable Integer id) {
        return Resp.ok({domain}Service.findById(id));
    }
}
```

필수 규칙:

1. SSR(`@Controller`)과 REST(`@RestController`)는 파일을 분리한다.
2. REST 엔드포인트는 `/api` 접두사를 사용한다.
3. SSR 컨트롤러는 `Service`, `HttpSession`을 생성자로 주입한다.
4. Mustache 컨트롤러 반환값은 `String`(템플릿 경로)만 사용한다.
5. REST 컨트롤러는 데이터만 반환한다(뷰 경로 문자열 반환 금지).
6. JSON 요청 본문 입력에는 `@RequestBody`를 사용한다.
7. `GET` 메서드에서는 `@RequestBody`를 사용하지 않는다.

---

## **8. 요청 DTO 규칙**

```java
public class {Domain}Request {

    @Data
    public static class Save {
        private String field1;
        private String field2;
    }

    @Data
    public static class Update {
        private String field1;
    }
}
```

필수 규칙:

1. 도메인당 `{Domain}Request.java` 파일 하나를 사용한다.
2. 외부 클래스에는 어노테이션을 선언하지 않는다.
3. 내부 static class 이름은 기능명(`Save`, `Update`, `Delete`, `Login`)을 사용한다.
4. `@Data`는 내부 static class에만 선언한다.

---

## **9. 응답 DTO 규칙**

```java
public class {Domain}Response {

    @Data
    public static class Detail {
        private Integer id;

        public Detail({Domain} entity) {
            this.id = entity.getId();
        }
    }

    @Data
    public static class Items {
        private Integer id;

        public Items({Domain} entity) {
            this.id = entity.getId();
        }
    }
}
```

필수 규칙:

1. 도메인당 `{Domain}Response.java` 파일 하나를 사용한다.
2. 외부 클래스에는 어노테이션을 선언하지 않는다.
3. 내부 static class 이름은 용도명(`Detail`, `Items`, `Summary`)을 사용한다.
4. Entity -> DTO 변환은 생성자 또는 정적 팩토리 메서드에서 처리한다.

---

## **10. 공통 응답 규칙 (Resp)**

```java
return Resp.ok(dto);
return Resp.fail(HttpStatus.BAD_REQUEST, "오류 메시지");
```

필수 규칙:

1. 위치는 `_core/util/Resp.java`를 사용한다.
2. REST API 응답은 `Resp<T>` 래퍼를 기본으로 사용한다.

---

## **11. 네이밍 규칙**

| 대상                 | 컨벤션             | 예시                |
| -------------------- | ------------------ | ------------------- |
| 클래스               | PascalCase         | `BoardService`      |
| 메서드/변수          | camelCase          | `findAll`, `userId` |
| 테이블명             | snake_case + `_tb` | `board_tb`          |
| 패키지               | lowercase          | `board`, `_core`    |
| 요청 DTO 내부 클래스 | 기능명             | `Save`, `Update`    |
| 응답 DTO 내부 클래스 | 용도명             | `Detail`, `Items`   |

---

## **12. 전역 제약**

| 항목          | 값/강제 사항                                                   |
| ------------- | -------------------------------------------------------------- |
| OSIV          | `false`                                                        |
| Fetch 전략    | `LAZY`만 사용                                                  |
| 배치 사이즈   | `default_batch_fetch_size=10`                                  |
| 인증 방식     | 기본은 `HttpSession` (별도 요청 없으면 Spring Security 미사용) |
| DTO 생성 위치 | Service 레이어                                                 |
| Entity 노출   | Controller에 Entity 직접 전달 금지                             |
