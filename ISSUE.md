# Issues Log

## Issue #001: 회원가입 시 서버 500 에러 발생

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

## Issue #002: 전역 예외 핸들러로 인한 디버깅 어려움

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

## Issue #003: 테스트 실행 시 SLF4J 로거 충돌로 ApplicationContext 로딩 실패

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

## Issue #004: SecurityConfig에 signup 엔드포인트 permitAll 누락

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

## Issue #005: 통합 테스트 간 SecurityContext 및 Redis 데이터 격리 실패

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

## Issue #006: Category 엔티티 도메인 메서드에서 캡슐화 위반

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

## Issue #007: Item-Category 양방향 ManyToMany 연관관계 구조 개선 필요

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