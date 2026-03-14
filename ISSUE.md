# Issues Log

## Issue #001: 공통 API 응답 객체(ApiResponse) 도입

### 배경
프로젝트 초기에는 Controller에서 반환 타입을 기본적인 Response Entity로 설정함

### 문제 상황
1. **일관성 없는 응답 형식**: 성공/실패 여부를 판단하는 기준이 API마다 달라 프론트엔드에서 분기 처리가 복잡해짐
2. **에러 응답 구조 부재**: 예외 발생 시 Spring 기본 에러 응답(Whitelabel Error Page 등)이 그대로 노출되어 클라이언트가 에러 메시지를 파싱하기 어려움
3. **GlobalExceptionHandler와의 연동 필요**: 전역 예외 핸들러에서도 동일한 형식으로 에러 응답을 내려줘야 하는데, 공통 응답 객체가 없으면 핸들러마다 별도의 응답 구조를 정의해야 함

### 해결 방법
제네릭 기반의 공통 응답 래퍼 클래스 `ApiResponse<T>` 도입

```java
@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
```

**적용 예시:**
```java
// Controller - 성공 응답
return ResponseEntity.ok(ApiResponse.success(orderService.createOrder(memberId, requestDto)));

// Controller - 메시지 포함 성공 응답
return ResponseEntity.ok(ApiResponse.success(orderId, "주문이 취소되었습니다."));

// GlobalExceptionHandler - 실패 응답
return ResponseEntity.status(code.getStatus())
        .body(ApiResponse.fail(code.getMessage()));
```

### 관련 파일
- `src/main/java/com/study/shop/global/response/ApiResponse.java`
- `src/main/java/com/study/shop/global/exception/GlobalExceptionHandler.java`

### 교훈
- **응답 일관성은 초기에 확립**: API가 늘어난 뒤 응답 형식을 통일하면 모든 Controller를 수정해야 하므로, 프로젝트 초기에 공통 응답 객체를 정의하는 것이 효율적
- **정적 팩토리 메서드 활용**: `success()`, `fail()` 정적 메서드로 생성을 단순화하여 Controller 코드의 가독성을 높임
- **제네릭으로 유연성 확보**: `ApiResponse<T>`로 어떤 타입의 데이터든 동일한 구조로 감쌀 수 있어 DTO 변경에 영향을 받지 않음
- **GlobalExceptionHandler와의 시너지**: 성공/실패 모두 `ApiResponse` 형식으로 통일되어 클라이언트는 항상 `success` 필드로 결과를 판단할 수 있음

---

## Issue #002: 회원가입 시 서버 500 에러 발생

**발생일**: 2025-09-22

### 문제 상황
프론트엔드에서 회원가입 폼 제출 시 서버 500 에러 발생

### 원인 분석
Member 엔티티의 password 필드에 설정된 `@Size(max = 50)` 검증 어노테이션이 bcrypt 암호화된 비밀번호(60자 이상)와 충돌

### 해결 방법
```java
// 수정 전
@Column(nullable = false, length = 50)
@Size(min = 6, max = 50)
private String password;

// 수정 후
@Column(nullable = false, length = 200)
private String password;
```

### 관련 파일
- `src/main/java/com/study/shop/domain/member/entity/Member.java`

### 교훈
- 암호화된 데이터의 길이 고려: bcrypt 등 해시 함수 사용 시 결과 길이를 충분히 고려
- 검증 어노테이션 신중 사용: 엔티티 필드의 검증 어노테이션은 비즈니스 로직과 데이터 처리 과정을 모두 고려

---

## Issue #003: 전역 예외 핸들러로 인한 디버깅 어려움

**발생일**: 2025-09-22

### 문제 상황
모든 예외가 전역 예외 핸들러에서 일괄 처리되어 구체적인 에러 정보 확인 불가

### 원인 분석
```java
// 문제가 된 코드
@ExceptionHandler
public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    return ResponseEntity.status(500)
            .body(ApiResponse.fail("서버 내부에 오류가 발생했습니다."));
}
```

### 해결 방법
```java
// 수정 후
@ExceptionHandler
public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    e.printStackTrace(); // 스택트레이스 출력 추가
    return ResponseEntity.status(500)
            .body(ApiResponse.fail("서버 내부에 오류가 발생했습니다."));
}
```

### 관련 파일
- `src/main/java/com/study/shop/global/exception/GlobalExceptionHandler.java`

### 교훈
- 디버깅 친화적인 예외 처리: 개발 중에는 구체적인 에러 정보를 확인할 수 있도록 로깅 필요

---

## Issue #004: 테스트 실행 시 SLF4J 로거 충돌로 ApplicationContext 로딩 실패

**발생일**: 2026-01-15

### 문제 상황
LoginControllerTest 등 통합 테스트 실행 시 ApplicationContext 로딩 실패로 모든 테스트 실패

### 오류 메시지
```
java.lang.IllegalStateException: Failed to load ApplicationContext
Caused by: java.lang.IllegalArgumentException: LoggerFactory is not a Logback LoggerContext but Logback is on the classpath.
Either remove Logback or the competing implementation (class org.slf4j.simple.SimpleLoggerFactory)
```

### 원인 분석
클래스패스에 두 개의 SLF4J 로거 구현체가 동시에 존재하여 충돌 발생:
- **Logback** (Spring Boot 기본 로거)
- **slf4j-simple** (embedded-redis 라이브러리의 전이 의존성)

의존성 트리 분석 결과:
```
+--- it.ozimov:embedded-redis:0.7.3
|    +--- org.slf4j:slf4j-simple:1.7.21 -> 2.0.16
```

### 해결 방법
build.gradle에서 embedded-redis의 slf4j-simple 의존성 제외:

```gradle
// 수정 전
testImplementation 'it.ozimov:embedded-redis:0.7.3'

// 수정 후
testImplementation('it.ozimov:embedded-redis:0.7.3') {
    exclude group: 'org.slf4j', module: 'slf4j-simple'
}
```

### 관련 파일
- `build.gradle`

### 교훈
- 전이 의존성 충돌 주의: 서드파티 라이브러리가 가져오는 전이 의존성이 프로젝트의 기본 설정과 충돌할 수 있음
- 의존성 분석 도구 활용: `./gradlew dependencies` 명령으로 의존성 트리를 분석하여 충돌 원인 파악 가능
- 라이브러리 선택 신중: 오래된 라이브러리(embedded-redis 0.7.3, 2017년 릴리즈)는 최신 Spring Boot와 호환성 문제 발생 가능

---

## Issue #005: SecurityConfig에 signup 엔드포인트 permitAll 누락

**발생일**: 2026-01-21

### 문제 상황
회원가입 API 테스트 시 인증 없이 접근할 수 없어 테스트 실패

### 원인 분석
SecurityConfig의 `authorizeHttpRequests`에서 `/api/auth/signup` 엔드포인트가 `permitAll()`에 포함되지 않아 인증이 필요한 상태로 설정됨

```java
// 문제가 된 코드
.requestMatchers(
    "/api/auth/login",
    "/api/auth/refresh",
    // ... swagger 관련 경로
).permitAll()
// /api/auth/signup이 누락됨
```

### 해결 방법
```java
// 수정 후
.requestMatchers(
    "/api/auth/signup",  // 추가
    "/api/auth/login",
    "/api/auth/refresh",
    // ... swagger 관련 경로
).permitAll()
```

### 관련 파일
- `src/main/java/com/study/shop/global/security/config/SecurityConfig.java`

### 교훈
- 인증이 필요 없는 공개 API(회원가입, 로그인 등)는 반드시 `permitAll()`에 명시적으로 추가
- 새로운 API 엔드포인트 추가 시 Security 설정 검토 필수

---

## Issue #006: 통합 테스트 간 SecurityContext 및 Redis 데이터 격리 실패

**발생일**: 2026-01-21

### 문제 상황
RefreshAndLogoutControllerTest에서 여러 테스트를 연속 실행 시 일부 테스트 실패
- 이전 테스트의 인증 정보가 SecurityContext에 남아 있어 다음 테스트에 영향
- 이전 테스트에서 저장한 Redis 데이터(refreshToken, blacklist)가 다음 테스트에 영향

### 원인 분석
`@Transactional`은 DB 롤백만 처리하고, SecurityContext와 Redis는 별도로 정리되지 않음
- SecurityContext: 스레드 로컬에 저장되어 테스트 간 공유됨
- Redis: 인메모리 저장소로 트랜잭션 롤백 대상이 아님

### 해결 방법
```java
@AfterEach
void tearDown() {
    // SecurityContext 초기화
    SecurityContextHolder.clearContext();

    // Redis 데이터 초기화
    stringRedisTemplate.getConnectionFactory().getConnection().flushDb();
}
```

### 관련 파일
- `src/test/java/com/study/shop/domain/auth/controller/RefreshAndLogoutControllerTest.java`

### 교훈
- `@Transactional`은 DB만 롤백: SecurityContext, Redis, 외부 시스템 등은 별도 정리 필요
- 테스트 격리 원칙: 각 테스트는 독립적으로 실행될 수 있어야 하며, 다른 테스트에 영향을 주거나 받지 않아야 함
- `@AfterEach` 활용: 테스트 후 상태 정리가 필요한 리소스는 명시적으로 초기화

---

## Issue #007: Category 엔티티 도메인 메서드에서 캡슐화 위반

**발생일**: 2026-02-01

### 문제 상황
Category 엔티티의 `addChild`, `removeChild` 도메인 메서드에서 `child.parent`에 직접 필드 접근하여 캡슐화를 위반하고 있음

### 원인 분석
같은 클래스 내부이기 때문에 컴파일 에러는 발생하지 않지만, 다른 객체의 private 필드에 직접 접근하는 것은 캡슐화 원칙에 위배됨

```java
// 문제가 된 코드
public void addChild(Category child) {
    this.child.add(child);
    child.parent = this;       // 직접 필드 접근 - 캡슐화 위반
}

public void removeChild(Category child) {
    this.child.remove(child);
    child.parent = null;       // 직접 필드 접근 - 캡슐화 위반
}
```

### 해결 방법
패키지 프라이빗 메서드를 활용하여 parent 변경을 캡슐화

```java
// 수정 후
public void addChild(Category child) {
    this.child.add(child);
    child.changeParent(this);
}

public void removeChild(Category child) {
    this.child.remove(child);
    child.changeParent(null);
}

// 패키지 프라이빗 - 외부 패키지에서는 접근 불가
void changeParent(Category parent) {
    this.parent = parent;
}
```

### 관련 파일
- `src/main/java/com/study/shop/domain/category/entity/Category.java`

### 교훈
- 같은 클래스라도 다른 인스턴스의 필드 직접 접근은 캡슐화 위반: Java는 같은 클래스 내 private 필드 접근을 허용하지만, 객체 간 캡슐화를 위해 메서드를 통한 접근이 바람직
- 패키지 프라이빗 메서드 활용: `changeParent()`를 패키지 프라이빗으로 두면 같은 패키지 내에서만 사용 가능하여, public setter 노출 없이 연관관계 관리 가능
- 양방향 연관관계 편의 메서드 설계 시 캡슐화 고려: JPA 양방향 매핑에서 편의 메서드 작성 시에도 객체지향 원칙을 지키는 방법을 선택

---

## Issue #008: Item-Category 양방향 ManyToMany 연관관계 구조 개선 필요

**발생일**: 2026-02-01

### 문제 상황
Item과 Category가 양방향 `@ManyToMany`로 연관관계를 맺고 있으며, 이는 일반적으로 지양되는 설계 패턴임

#### 현재 구조
```java
// Category 엔티티
@ManyToMany
@JoinTable(name = "category_item",
        joinColumns = @JoinColumn(name = "category_id"),
        inverseJoinColumns = @JoinColumn(name = "item_id"))
private List<Item> items = new ArrayList<>();
```

### 해결방법: 단방향 ManyToMany로의 전환

**문제점:**

1. **성능 문제** - Category → Item 조회 시 항상 조인 테이블을 거쳐야 하며, N+1 문제 발생 가능성
2. **코드 복잡성 증가** - Repository에서 복잡한 JPQL/QueryDSL 필요, 직관적이지 않은 조회 로직
3. **캐시 효율성 저하** - Category 기준 Item 목록 캐싱 어려움
4. **ORM 최적화 제한** - JPA의 양방향 최적화 기능 활용 불가

- 특정 카테고리로 Item 검색 쿼리:
```sql
SELECT i.* FROM item i
JOIN category_item ci ON i.item_id = ci.item_id
WHERE ci.category_id = ?
```

### 검토한 대안들

#### 1. 중간 엔티티 생성
```java
@Entity
public class CategoryItem {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    // 추가 속성 가능 (등록일, 순서 등)
    private LocalDateTime createdAt;
}
```

#### 2. 단방향 + Repository 메서드
```java
// ItemRepository
List<Item> findByCategoriesId(Long categoryId);

// CategoryService
public List<Item> getItemsByCategory(Long categoryId) {
    return itemRepository.findByCategoriesId(categoryId);
}
```

#### 3. QueryDSL 활용
```java
public List<Item> findItemsByCategory(Long categoryId) {
    return queryFactory
        .selectFrom(item)
        .join(item.categories, category)
        .where(category.id.eq(categoryId))
        .fetch();
}
```

### 결정 사항
현재는 ManyToMany 양방향 관계로 개발을 진행하되, 추후 중간 엔티티 방식으로 리팩토링 예정

### 관련 파일
- `src/main/java/com/study/shop/domain/category/entity/Category.java`
- `src/main/java/com/study/shop/domain/Item/entity/Item.java`

### 교훈
- 양방향 `@ManyToMany` 지양: 중간 테이블을 직접 제어할 수 없고, 추가 컬럼 확장이 불가능하여 실무에서는 중간 엔티티 방식이 권장됨
- 단방향으로도 충분히 해결 가능: Repository 메서드나 QueryDSL을 활용하면 양방향 없이도 양쪽 방향의 조회가 가능
- 설계 결정은 단계적으로: 초기 개발 시에는 단순한 구조로 시작하고, 필요에 따라 리팩토링하는 접근도 유효

**상태:** 미해결 (추후 리팩토링 예정)

---

## Issue #009: Order 엔티티 접근 제어자를 활용한 DDD 캡슐화 설계

**발생일**: 2026-02-13

### 배경
Order 도메인 엔티티를 설계하면서 메서드의 접근 제어자를 역할에 따라 구분하여 DDD 원칙을 준수하는 캡슐화 전략을 적용

### 적용한 접근 제어자 전략

#### 1. `public` - 핵심 비즈니스 로직 (외부 레이어에서 호출)
```java
// Order.java - 주문 생성
public Order create(Member member, OrderItem... orderItems) { ... }

// Order.java - 주문 취소
public void cancel() {
    if (this.orderStatus != OrderStatus.PENDING) {
        throw new IllegalStateException("주문 수락 전에만 취소할 수 있습니다.");
    }
    this.orderStatus = OrderStatus.CANCELLED;
}

// OrderItem.java - 주문 항목 생성, 취소
public static OrderItem create(Order order, Item item, Integer quantity) { ... }
public void cancel() { ... }
```

**장점:**
- Service 레이어에서 명확하게 호출 가능하여 비즈니스 유스케이스를 직관적으로 표현
- 도메인 모델의 공개 API 역할을 하여 엔티티가 어떤 행위를 할 수 있는지 명확히 드러남
- 테스트 작성이 용이 (외부에서 직접 호출 가능)

**단점:**
- 어디서든 호출 가능하므로 의도하지 않은 곳에서 사용될 위험
- 공개 범위가 넓어 변경 시 영향 범위가 큼

#### 2. 패키지 프라이빗 (default) - 연관관계 편의 메서드 (같은 도메인 패키지 내에서만 호출)
```java
// Order.java
void assignMember(Member member) {
    this.member = member;
    member.getOrders().add(this);
}

void addOrderItem(OrderItem orderItem) {
    this.orderItems.add(orderItem);
    orderItem.assignOrder(this);
}

// OrderItem.java
void assignOrder(Order order) {
    this.order = order;
}
```

**장점:**
- 같은 `domain.order.entity` 패키지 내의 엔티티끼리만 호출 가능하여 도메인 내부 협력을 안전하게 캡슐화
- Service 레이어에서 직접 호출 불가 → 연관관계 설정을 반드시 엔티티의 public 메서드를 통해 수행하도록 강제
- public setter 노출 없이 양방향 연관관계 동기화 가능 (Issue #007의 교훈 적용)

**단점:**
- 같은 패키지에 다른 클래스가 추가되면 의도치 않게 접근 가능
- Java의 패키지 프라이빗은 하위 패키지까지 적용되지 않음 (예: `entity.sub` 패키지에서는 접근 불가)
- 접근 제어자가 명시적으로 보이지 않아 코드 리뷰 시 놓칠 수 있음

#### 3. `private` - 내부 검증 로직 (엔티티 자기 자신만 호출)
```java
// 예시: 주문 상태 검증, 가격 계산 등 내부 로직
private void validateOrderStatus() {
    if (this.orderStatus != OrderStatus.PENDING) {
        throw new IllegalStateException("주문 수락 전에만 취소할 수 있습니다.");
    }
}
```

**장점:**
- 완전한 캡슐화: 외부에서 절대 접근 불가하여 내부 구현 변경이 자유로움
- 검증 로직이나 계산 로직을 분리하여 public 메서드의 가독성 향상
- 변경 시 영향 범위가 해당 엔티티로 한정

**단점:**
- 테스트에서 직접 호출 불가하여 public 메서드를 통해 간접적으로만 테스트 가능
- 과도하게 사용하면 같은 도메인 내 엔티티 간 협력이 어려워질 수 있음

### 접근 제어자별 역할 요약

| 접근 제어자 | 역할 | 호출 범위 | 예시 |
|---|---|---|---|
| `public` | 핵심 비즈니스 로직 | Service, 외부 레이어 | `create()`, `cancel()` |
| 패키지 프라이빗 | 연관관계 편의 메서드 | 같은 패키지 내 엔티티 | `assignMember()`, `addOrderItem()` |
| `private` | 내부 검증/계산 로직 | 엔티티 자기 자신 | `validateOrderStatus()` |

### 관련 파일
- `src/main/java/com/study/shop/domain/order/entity/Order.java`
- `src/main/java/com/study/shop/domain/order/entity/OrderItem.java`

### 교훈
- **접근 제어자는 설계 의도를 표현하는 도구**: 단순히 컴파일 에러를 막기 위한 것이 아니라, 각 메서드의 역할과 호출 범위를 명확히 전달하는 수단
- **DDD에서 엔티티는 자신의 상태를 스스로 관리**: public 메서드로 비즈니스 행위를 노출하고, 내부 상태 변경은 패키지 프라이빗 또는 private으로 보호
- **패키지 구조가 접근 제어의 핵심**: `domain.order.entity` 패키지에 Order와 OrderItem을 함께 두어 패키지 프라이빗의 이점을 최대한 활용
- **Issue #007의 연장선**: Category에서 배운 캡슐화 원칙을 Order 도메인에도 일관되게 적용

---

## Issue #010: Post.removeComment()에서 연관관계만 끊어지고 실제 삭제가 이루어지지 않는 문제

**발생일**: 2026-02-20

### 문제 상황
`Post.removeComment()` 메서드 호출 시 컬렉션에서 Comment를 제거하고 `comment.setPost(null)`로 연관관계를 끊지만, Comment 엔티티 자체는 DB에서 삭제되지 않음

#### 현재 코드
```java
public void removeComment(Comment comment) {
    this.comments.remove(comment);
    comment.setPost(null);  // 연관관계만 끊김, DB에서 삭제되지 않음
}
```

### 원인 분석
`comment.setPost(null)`은 Comment 엔티티의 외래키(post_id)를 null로 설정할 뿐, Comment 레코드 자체를 삭제하지 않음. 이로 인해:
1. **내가 작성한 댓글 조회 시 삭제된 댓글이 여전히 조회됨**: `CommentRepository.findActiveCommentByMemberId()`는 `deleted = false`인 댓글을 조회하는데, `removeComment()`는 `deleted` 플래그를 변경하지 않으므로 해당 댓글이 계속 조회됨
2. **고아 데이터 발생**: post_id가 null인 Comment가 DB에 남아 데이터 정합성 저하

### 검토한 대안

#### 1. orphanRemoval 활용
Post 엔티티의 `@OneToMany`에 이미 `orphanRemoval = true`가 설정되어 있으므로, `comments.remove(comment)` 호출 시 JPA가 자동으로 Comment를 DB에서 삭제함. 단, `comment.setPost(null)` 없이 컬렉션에서 제거만 하면 orphanRemoval이 동작함

```java
public void removeComment(Comment comment) {
    this.comments.remove(comment);
    // orphanRemoval = true이므로 컬렉션에서 제거되면 JPA가 자동 DELETE
}
```

#### 2. 소프트 삭제
Comment 엔티티의 `deleted` 플래그를 활용하여 논리적 삭제 수행. 데이터 보존이 필요한 경우에 적합

```java
public void removeComment(Comment comment) {
    comment.markAsDeleted();  // deleted = true로 변경
}
```

### 관련 파일
- `src/main/java/com/study/shop/domain/post/entity/Post.java`
- `src/main/java/com/study/shop/domain/comment/repository/CommentRepository.java`

### 교훈
- **연관관계 해제 ≠ 삭제**: `setPost(null)`은 FK를 null로 설정할 뿐 레코드를 삭제하지 않으므로, 삭제 의도라면 orphanRemoval 또는 명시적 삭제가 필요
- **orphanRemoval과 연관관계 편의 메서드의 조합 주의**: orphanRemoval이 설정된 상태에서 `setPost(null)`을 먼저 호출하면 JPA가 orphan 감지를 제대로 하지 못할 수 있음
- **소프트 삭제와 하드 삭제 전략 통일**: 프로젝트 내에서 삭제 전략을 일관되게 유지해야 조회 로직의 혼란을 방지할 수 있음

**상태:** 미해결 (소프트 삭제 방안 적용 고려중)

---

## Issue #011: Order-OrderItem 양방향 연관관계의 순환 의존성 문제

**발생일**: 2026-02-23

### 배경
`OrderService`에서 주문 생성 로직을 구현하는 과정에서 Order와 OrderItem이 양방향 연관관계를 맺고 있어 생성 순서에 대한 순환 의존성이 발생함.

### 문제 상황
```
OrderItem 생성 → Order 참조 필요 (FK: order_id)
Order 생성 → OrderItem 참조 필요 (비즈니스 로직)
```

두 엔티티가 서로를 참조하는 구조로 인해 어느 것을 먼저 생성해야 할지 결정하지 못하는 전형적인 순환 의존성 문제였음.

### 검토한 방안

"먼저 생성하고 나중에 연관관계 설정" 3단계 패턴:

```java
// 1. OrderItem을 먼저 생성 (Order 없이)
List<OrderItem> orderItems = requestDto.getOrderItems().stream()
    .map(dto -> {
        Item item = itemRepository.findById(dto.getItemId());
        return OrderItem.createOrderItem(item, dto.getQuantity());
    })
    .collect(toList());

// 2. Order 생성
Order order = Order.createOrder(member, requestDto.getAddress());

// 3. 연관관계 나중에 설정
orderItems.forEach(order::addOrderItem);
```


**실제 구현 코드:**

```java
// OrderItem.java - Order 없이 생성하는 정적 팩토리
public static OrderItem create(Item item, Integer quantity) {
    if (item.getStock() < quantity) {
        throw new StockNotEnoughException(item.getId()); // 재고 검증 추가
    }
    item.removeStock(quantity);
    return OrderItem.builder()
            .item(item)
            .quantity(quantity)
            .price(item.getPrice()) // 주문 당시 가격 스냅샷 추가
            .build();
    // order 필드는 설정하지 않음
}

// 패키지 프라이빗 - Order.addOrderItem() 에서만 호출 가능
void assignOrder(Order order) {
    this.order = order;
}

// Order.java - 연관관계 진입점
public void addOrderItem(OrderItem orderItem) {
    this.orderItems.add(orderItem);
    orderItem.assignOrder(this); // 패키지 프라이빗 메서드 호출
}

// OrderService.java - 실제 서비스 레이어
List<OrderItem> orderItems = requestDto.getOrderItems().stream()
    .map(dto -> {
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException(dto.getItemId()));
        return OrderItem.create(item, dto.getQuantity()); // 1단계: Order 없이 생성
    }).toList();

Order order = Order.create(member, requestDto.getAddress()); // 2단계: Order 생성
orderItems.forEach(order::addOrderItem); // 3단계: 연관관계 설정
```

### 대안 분석

#### 대안 1: 단방향 연관관계로 변경
```java
// OrderItem이 Order를 참조하지 않는 구조
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
@JoinColumn(name = "order_id") // Order가 FK 주인
private List<OrderItem> orderItems;
```

**장점:**
- 순환 의존성 문제 자체가 사라짐
- 코드 단순화

**단점:**
- `@OneToMany`에 `@JoinColumn`을 사용하면 INSERT 후 별도의 UPDATE SQL이 추가 발생하는 JPA 특성 문제 (성능 저하)
- OrderItem → Order 역탐색 불가 (별도 Repository 쿼리 필요)

#### 대안 2: 중간 단계 저장 (지연 초기화)
```java
// OrderItem 먼저 저장 후 Order에 추가
OrderItem savedItem = orderItemRepository.save(
    OrderItem.builder().item(item).quantity(qty).build()
);
order.addOrderItem(savedItem);
```

**장점:** 구현이 직관적이고 단순함

**단점:**
- 불완전한 상태(order_id = null)의 OrderItem이 DB에 저장될 위험
- 불필요한 UPDATE SQL 추가 발생
- 트랜잭션 내 중간 저장이 복잡성을 높임

#### 대안 3: 생성 메서드에 Order를 직접 전달 (원래 시도했던 방식)
```java
// 문제가 된 방식 - 순환 의존성
public static OrderItem create(Order order, Item item, Integer quantity) {
    // Order 생성 시 OrderItem 필요, OrderItem 생성 시 Order 필요 → 교착 상태
}
```

**결론:** 양쪽 모두 상대방을 먼저 요구하므로 근본적으로 해결 불가. 채택 방식(Order 없이 생성 → 나중에 연관관계 설정)이 이 제약을 우회하는 유일한 실용적 해결책임.

#### 대안 4: 현재 채택 방식 - "먼저 생성, 나중에 연관관계 설정"

**장점:**
- 직관적이고 가독성 높음
- JPA 영속성 전이(`cascade = ALL`)를 통해 Order 저장 시 OrderItem 자동 저장
- `orphanRemoval`과 함께 엔티티 생명주기 자동 관리
- 패키지 프라이빗 `assignOrder()`로 외부 레이어의 직접 조작 차단

**단점:**
- OrderItem 생성 시점과 연관관계 설정 시점이 분리되어 중간에 불완전한 상태(order = null)로 존재
- `assignOrder()`의 이점을 살리려면 Order와 OrderItem이 같은 패키지에 위치해야 함

### 실제 구현의 추가 개선 사항

제안된 해결책에서 실제 구현으로 오면서 다음이 추가됨:

1. **재고 검증 (`StockNotEnoughException`)**: `OrderItem.create()` 내에서 생성 시점에 즉시 재고 부족 여부를 검증하여 생성 후 롤백 없이 빠른 실패(fail-fast) 처리
2. **가격 스냅샷**: 주문 당시의 `item.getPrice()`를 `price` 필드로 캡처하여 이후 상품 가격 변경 시에도 주문 금액의 데이터 정합성 유지
3. **패키지 프라이빗 `assignOrder()`**: Issue #009의 교훈 적용 - Service 레이어에서 직접 연관관계 조작 불가, 반드시 `order.addOrderItem()`을 통해서만 설정 가능

### 관련 파일
- `src/main/java/com/study/shop/domain/order/entity/Order.java`
- `src/main/java/com/study/shop/domain/order/entity/OrderItem.java`
- `src/main/java/com/study/shop/domain/order/service/OrderService.java`

### 교훈
- **"먼저 생성, 나중에 연관관계 설정" 패턴**: JPA 양방향 연관관계의 순환 의존성은 한쪽을 먼저 생성(빈 상태)하고, 편의 메서드(`addOrderItem`)로 나중에 연결하는 방식으로 해결
- **패키지 프라이빗과 순환 의존성 해결의 시너지**: 연관관계 설정 메서드를 패키지 프라이빗으로 관리하면 Service가 직접 조작할 수 없어 자연스럽게 올바른 생성 순서가 강제됨
- **팩토리 메서드에서 불변 데이터 캡처**: 가격 등 시간이 지나면 변할 수 있는 데이터는 생성 시점에 스냅샷으로 저장하여 데이터 정합성 유지 (e-커머스의 기본 원칙)
- **Issue #009와의 연계**: 접근 제어자 전략(public/패키지 프라이빗/private 분리)이 순환 의존성 해결과 자연스럽게 결합되어 더 나은 설계로 발전

**상태:** 해결됨

---

## [IMPORTANT] Issue #012: N+1 문제 해결 과정 — findAllPosts 메서드 개선

**작성일**: 2026-03-14
**관련 작업**: `findAllPosts` 메서드 개선 (JpaRepository → QueryDSL + Paging + DTO Projection)

---

### 배경

모든 Post를 조회하는 `findAllPosts` 메서드를 단순 JpaRepository 방식에서, N+1 문제를 해결하고 페이징 및 댓글 수 집계 기능을 제공하는 방식으로 단계적으로 개선함.

---

### Inner Join vs Left Join 선택 기준

- **Left Join**: 연관 엔티티가 없는 경우에도 기준 엔티티 조회 가능 → 항상 안전
- **Inner Join**: 연관 엔티티가 반드시 존재함이 보장될 때 사용

**Post → Member 경우:**
- 일반적으로 게시글에는 작성자가 반드시 존재 → Inner Join 안전
- 단, 탈퇴 회원을 Hard Delete로 처리하되 Cascade 삭제 미적용 구조라면 `post.member`가 `null`이 될 수 있으므로 → Left Join 필요

---

### N:1 vs 1:N Fetch Join의 차이

**N:1 조회 (예: Post → Member)**
- Join 결과 row 수 = Post 수와 동일 → Cartesian Product 없음
- Fetch Join + Paging 함께 사용 안전

**1:N 조회 (예: Post → Comments, Collection Fetch Join)**
- Join 시 결과 row가 Comment 수만큼 증가 → Cartesian Product 발생
- **Join 결과 row가 자식 엔티티(Comment) 수 N배만큼 증가**
- JPA는 이 경우 **페이징을 메모리에서 처리**하여 OOM 위험 발생
  → Hibernate 경고: `HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory`
- **따라서 Collection Fetch Join + Paging은 일반적으로 금지**

**Collection Fetch Join + Paging이 꼭 필요한 경우 대안:**
1. **2단계 조회**: 부모(Post)를 먼저 Paging 조회 후, 자식(Comment)을 `IN` 조건으로 별도 조회
2. **Batch Size**: `@BatchSize` 또는 `hibernate.default_batch_fetch_size` 설정으로 N+1을 IN 쿼리로 최적화
3. **DTO Projection**: 집계 함수(`count()`)로 필요한 값만 선택 조회 → 이 방법을 최종 채택

> 참고: 1:N Fetch Join이 필요한 단건 조회는 애초에 Paging이 필요한 상황이 거의 없다.

---

### 쿼리 설계 원칙

> **비즈니스 요구사항(필요한 데이터)을 먼저 파악하고, 그에 맞는 쿼리 전략을 선택하는 것이 가장 중요하다.**

| 상황 | 필요 데이터 | 페이징 | 적합한 전략 |
|---|---|---|---|
| Post 목록 조회 | Post, Member, 댓글 수 | 필요 | N:1 Join + DTO Projection + Paging |
| Post 단건 조회 | Post, Comments, Member | 불필요 | 1:N Fetch Join 또는 2단계 조회 |

---

### Count Query 분리의 중요성

→ **페이징(Page) 사용 시 content 쿼리와 count 쿼리를 분리해야, 집계에 불필요한 Join/orderBy를 제거하여 성능을 최적화할 수 있다.**

- content 쿼리와 count 쿼리의 `from`, `join`, `where` 절은 기본적으로 일치시켜야 하지만,
- **집계 목적상 불필요한 join이나 orderBy는 count 쿼리에서 생략 가능**

```java
Long total = queryFactory
    .select(post.count())
    .from(post)
    // .join(post.member, member)
    // ↑ member가 반드시 존재한다는 보장 하에 생략 가능
    //   보장이 없으면 left join 필요 → 구조 복잡도 증가
    .fetchOne();
```

---

### 단계별 코드 개선 이력

#### 1단계: JPA @Query (초기)

```java
@Query(value = "select p from Post p join fetch p.member",
       countQuery = "select count(p) from Post p")
Page<Post> findAllWithMember(Pageable pageable);
```

- N+1 해결 + 페이징 지원
- 동적 조건 추가 어려움, 댓글 수 집계 불가

#### 2단계: QueryDSL 전환

```java
@Override
public List<PostListDto> findAllPosts() {
    return queryFactory
        .selectFrom(post)
        .join(post.member, member)
        .orderBy(post.createdAt.desc())
        .fetch();
}
```

- 타입 안전성, 컴파일 타임 오류 감지, 동적 조건 조합 가능
- 페이징 및 댓글 수 집계 여전히 미지원

#### 3단계: QueryDSL + DTO Projection + Paging (최종)

```java
@Override
public Page<PostListDto> findAllPosts(Pageable pageable) {
    List<PostListDto> content = queryFactory
        .select(Projections.constructor(PostListDto.class,
            post.id, post.title, post.content, post.createdAt, comment.count()
        ))
        .from(post)
        .join(post.member, member)
        .leftJoin(post.comments, comment) // 댓글 없는 게시글도 포함 → leftJoin
        .groupBy(post.id)
        .orderBy(post.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long total = queryFactory
        .select(post.count())
        .from(post)
        .fetchOne();

    return new PageImpl<>(content, pageable, total);
}
```

> **왜 이 코드는 Post → Comments(1:N) + Paging임에도 안전한가?**
>
> `Fetch Join`이 아닌 **일반 Join + `groupBy` + `count()` 구조**이기 때문.
> - Fetch Join: 연관 엔티티 객체 전체를 영속성 컨텍스트에 로딩 → row 뻥튀기 발생, JPA가 메모리 페이징 시도
> - 일반 Join + groupBy: SQL 집계 결과를 DTO로 받음 → row 뻥튀기 없음, DB 레벨 페이징 안전

---

### Fetch Join vs 일반 Join 정리

| 구분 | Fetch Join | 일반 Join |
|---|---|---|
| 목적 | 연관 엔티티 객체를 영속성 컨텍스트에 로딩 | 집계/필터 조건으로 연관 테이블 활용 |
| 반환 타입 | Entity | DTO (Projection) |
| 1:N + Paging | 위험 (메모리 페이징 경고, OOM 가능) | 안전 (groupBy + 집계 함수 활용 시) |

---

### 핵심 요약

1. **N:1 Fetch Join + Paging → 안전**
2. **1:N (Collection) Fetch Join + Paging → 위험, 대안 필요**
3. **일반 Join + groupBy + 집계 함수 → 1:N이라도 Paging 안전**
4. **Count Query는 content 쿼리와 분리하여 불필요한 Join 제거로 성능 최적화**
5. **비즈니스 요구사항 → 필요 데이터 파악 → 쿼리 전략 선택 순서로 설계**

**상태:** 해결됨