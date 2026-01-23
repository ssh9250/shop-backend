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