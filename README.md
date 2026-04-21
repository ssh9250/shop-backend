# 🛒 Shop — C2C 중고거래 플랫폼

> Spring Boot 기반 C2C 중고거래 백엔드 API 서버입니다.  
> 단순 기능 구현을 넘어 **설계 판단의 근거**와 **트레이드오프**를 코드에 담는 데 집중했습니다.

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-6DB33F?style=flat-square&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-7.2-DC382D?style=flat-square&logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [기술 스택](#2-기술-스택)
3. [아키텍처 및 도메인 설계](#3-아키텍처-및-도메인-설계)
4. [핵심 설계 판단](#4-핵심-설계-판단)
5. [동시성 제어 실험](#5-동시성-제어-실험)
6. [트러블슈팅](#6-트러블슈팅)
7. [실행 방법](#7-실행-방법)
8. [API 명세](#8-api-명세)

---

## 1. 프로젝트 개요

개인 간 중고 물품 거래를 지원하는 RESTful API 서버입니다.  
회원가입부터 상품 등록, 주문, 관리자 기능까지 실서비스 수준의 도메인을 구현했습니다.

**구현 도메인:** 인증(Auth) · 회원(Member) · 게시글(Post) · 댓글(Comment) · 상품(Item) · 주문(Order) · 관리자(Admin)

**프로젝트 목표:**
- 기능 동작을 넘어 **왜 그렇게 설계했는가**를 설명할 수 있는 코드 작성
- v1(Object Reference) → v2(ID Reference) 리팩터링 계획을 통한 DDD 설계 이해 심화
- 실제 부하 테스트로 동시성 전략의 트레이드오프를 수치로 검증

---

## 2. 기술 스택

| 분류 | 기술 |
|---|---|
| Language / Framework | Java 17, Spring Boot 3.3.5 |
| ORM / Query | Spring Data JPA, QueryDSL 5.0.0 |
| Security | Spring Security, JWT (jjwt 0.11.5) |
| Cache | Redis 7.2 (Refresh Token, Write-behind 조회수) |
| Database | MySQL 8.0 (운영/로컬), H2 (테스트) |
| 동시성 제어 | `@Version` 낙관적 락, spring-retry `@Retryable` |
| 이벤트 | Spring Events + `@Async` |
| 인프라 | Docker Compose (MySQL · Redis · App 3-tier) |
| 문서화 | springdoc-openapi (Swagger UI) |
| 테스트 | MockMvc `@SpringBootTest`, JaCoCo |

---

## 3. 아키텍처 및 도메인 설계

### 패키지 구조

```
src/main/java/com/example/shop/
├── domain/
│   ├── auth/          # JWT 인증, Refresh Token Rotation
│   ├── member/        # 회원 관리, 탈퇴 처리
│   ├── post/          # 게시글 CRUD, 파일 업로드
│   ├── comment/       # 댓글, 소프트 삭제
│   ├── item/          # 상품, 커서 페이징, 재고 관리
│   └── order/         # 주문 상태 머신, 재고 연동
├── admin/             # 관리자 전용 (ROLE_ADMIN)
├── global/
│   ├── exception/     # GlobalExceptionHandler, ErrorCode
│   ├── response/      # ApiResponse<T> 통일 응답
│   └── config/        # Security, QueryDSL, Redis, Async
└── security/          # JwtAuthenticationFilter
```

### 인증 흐름

```
클라이언트                         서버
   │                                │
   │── POST /auth/login ──────────►│
   │                                │ Access Token (30분, HS256)
   │◄── { accessToken,             │ Refresh Token (14일, Redis)
   │      refreshToken } ──────────│
   │                                │
   │── API 요청 (Bearer Token) ───►│ JwtAuthenticationFilter
   │                                │   → SecurityContext 등록
   │── POST /auth/refresh ────────►│ Refresh Token 검증
   │                                │   → Token Rotation (구 토큰 블랙리스트)
   │◄── { 새 accessToken } ────────│
   │                                │
   │── POST /auth/logout ─────────►│ Access Token → Redis 블랙리스트
```

### 주문 상태 머신

```
PENDING ──► ORDERED ──► IN_DELIVERY ──► COMPLETED
   │           │               │
   └───────────┴───────────────┘
               CANCELLED (재고 복구)
```

`OrderStatus` enum에 `next()`, `canCancel()` 메서드를 캡슐화하여  
서비스 레이어가 상태 전이 규칙을 직접 알지 않아도 되도록 설계했습니다.

```java
// OrderStatus enum 내부
public OrderStatus next() {
    return switch (this) {
        case PENDING -> ORDERED;
        case ORDERED -> IN_DELIVERY;
        case IN_DELIVERY -> COMPLETED;
        default -> throw new InvalidOrderStatusException(this);
    };
}
```

---

## 4. 핵심 설계 판단

### 4-1. v1 → v2: Object Reference에서 ID Reference로

이 프로젝트의 가장 핵심적인 설계 고민입니다.

**v1 현재 구조 (Object Reference)**

```java
@Entity
public class Post {
    @ManyToOne(fetch = LAZY)
    private Member member;   // 객체 직접 참조
}

@Entity
public class Order {
    @ManyToOne(fetch = LAZY)
    private Member member;   // Aggregate 경계 없이 참조
}
```

JPA를 처음 사용할 때 자연스러운 선택이지만, 이 방식은 다음 문제를 안고 있습니다.

- **Aggregate 경계 붕괴**: `Post`에서 `member.getOrders()`처럼 다른 Aggregate를 무제한 탐색 가능
- **영속성 컨텍스트 의존**: 객체 그래프 탐색이 쿼리 발생 시점을 불투명하게 만듦
- **MSA 전환 불가**: 서비스 분리 시 JPA 연관관계가 물리적 장벽이 됨

**v2 계획 (ID Reference)**

```java
@Entity
public class Post {
    private Long memberId;   // FK만 저장, 객체 참조 없음
    // 작성자 정보가 필요하면: memberRepository.findById(memberId)
}

@Entity
public class Order {
    private Long memberId;   // 명시적 조회로 쿼리 발생 지점 명확화
}
```

| 항목 | v1 (Object Reference) | v2 (ID Reference) |
|---|---|---|
| Aggregate 경계 | 불명확 (자유로운 탐색) | 명확 (ID로만 접근) |
| 쿼리 발생 시점 | 암묵적 (Lazy 로딩) | 명시적 (Repository 호출) |
| MSA 전환 | 어려움 | 용이 |
| 코드 복잡도 | 낮음 (편리) | 약간 높음 |

> **v1을 먼저 완성한 이유**: Object Reference의 편의성과 한계를 직접 경험한 후  
> ID Reference로 전환하면, 설계 판단의 근거가 추상적 이론이 아닌 실제 경험에서 나옵니다.  
> `shop-v2` 브랜치에서 리팩터링을 진행할 예정입니다.

---

### 4-2. 소프트 삭제 — Cascade 제거, 명시적 처리

**문제**: `@SQLDelete` + `@SQLRestriction`은 SQL을 가로채는 방식이라,  
부모 엔티티 삭제 시 CascadeType으로 자식 soft delete가 트리거되지 않습니다.

```java
// 동작하지 않는 방식
@OneToMany(cascade = CascadeType.ALL)  // → soft delete 미적용
private List<Post> posts;
```

**해결**: cascade 제거 + 서비스 레이어에서 명시적 처리

```java
// MemberService.withdraw()
public void withdraw(Long memberId) {
    postRepository.softDeleteByMemberId(memberId);      // 게시글 soft delete
    commentRepository.softDeleteByMemberId(memberId);   // 댓글 soft delete
    itemRepository.softDeleteByMemberId(memberId);      // 상품 soft delete
    // 주문(Order)은 거래 기록 보존을 위해 삭제하지 않음
    memberRepository.softDeleteById(memberId);
}
```

> `Order`를 삭제하지 않는 이유: 탈퇴 회원과 거래한 상대방의 거래 내역이 소멸되면 안 됩니다.  
> `sellerId`(Long), `buyerEmail`(String) 스냅샷을 Order에 보존하여 탈퇴 후에도 조회 가능하게 했습니다.

**v2 개선 방향**: Spring Events로 `MemberWithdrawnEvent`를 발행하면  
각 도메인이 스스로 처리하는 이벤트 기반 구조로 개선 가능합니다.  
현재 `PostViewedEvent`, `OrderAcceptedEvent`로 Spring Events를 이미 도입했습니다.

---

### 4-3. 페이징 전략 — 게시글(Page) vs 상품(Cursor)

두 도메인에 의도적으로 다른 페이징 전략을 적용했습니다.

| | 게시글 (Post) | 상품 (Item) |
|---|---|---|
| 방식 | Offset 기반 `Page<T>` | Cursor 기반 `Slice<T>` |
| 이유 | 특정 페이지 이동, 총 개수 필요 | 무한 스크롤, 실시간 데이터 추가 |
| 커서 구성 | — | `lastCreatedAt + lastId` 복합 커서 |
| N+1 해결 | QueryDSL Projection | QueryDSL seller fetchJoin |

커서 페이징에서 `loe` 대신 `lt`를 사용한 이유:  
동일 `createdAt`에 여러 건이 있을 때 `loe`는 중복 노출이 발생합니다.  
`(createdAt < lastCreatedAt) OR (createdAt = lastCreatedAt AND id < lastId)` 조건으로 strict ordering을 보장합니다.

---

### 4-4. Redis 캐싱 범위 결정

**시도 → 포기**: `Page<PostListDto>` Redis 캐싱

`PageImpl`에 기본 생성자가 없어 Jackson 역직렬화 실패 → `CachePage<T>` 커스텀 DTO 도입 →  
`LocalDateTime` 직렬화 실패 → `JavaTimeModule` 등록 → `DefaultTyping` 설정 필요...

우회책이 쌓이는 시점에 **"이 복잡도가 캐싱 이득보다 큰가"** 를 판단했고, 목록 캐싱을 제거했습니다.

**유지**: 조회수 Write-behind 패턴  
- 조회 시 Redis `INCR` (O(1))
- `@Scheduled`로 30분마다 MySQL에 일괄 반영
- IP 기반 중복 조회 방지 (`@Async` 비동기 처리)

---

## 5. 동시성 제어 실험

**시나리오**: 재고 1개인 상품에 50명이 동시에 주문 요청 (k6, 10초)

### 결과 비교

| | 락 없음 | 낙관적 락 (H2) | 낙관적 락 (MySQL) | 비관적 락 |
|---|---|---|---|---|
| **성공률 (201)** | 21% | 93% | 63% | 56%* |
| **충돌/오류 (409)** | 500 발생 | 6% | 36% | 0% |
| **평균 응답시간** | 빠름 | ~2ms | ~2ms | ~70ms |
| **데이터 정합성** | ❌ 미보장 | ✅ 보장 | ✅ 보장 | ✅ 보장 |
| **데드락 발생** | ✅ 있음 | ❌ 없음 | ✅ 있음 | ❌ 없음 |

*재고 소진으로 인한 400 포함

### 핵심 발견

**발견 1: H2와 MySQL의 동시성 동작이 다르다**  
H2에서 93% 성공하던 낙관적 락이 MySQL에서 63%로 낮아졌습니다.  
MySQL InnoDB의 갭 락·넥스트 키 락이 `@Version` 감지 전에 데드락을 유발합니다.  
→ **부하 테스트는 반드시 실제 운영 DB와 동일한 환경에서 해야 합니다.**

**발견 2: 낙관적 락 + MySQL 데드락 해결**

```java
@Retryable(
    retryFor = {
        ObjectOptimisticLockingFailureException.class,
        CannotAcquireLockException.class   // 데드락도 재시도 대상
    },
    noRetryFor = {
        StockNotEnoughException.class      // 비즈니스 예외는 재시도 제외
    },
    maxAttempts = 5,
    backoff = @Backoff(delay = 100)
)
```

`StockNotEnoughException`을 `noRetryFor`로 명시하지 않으면  
spring-retry가 `@Recover` 메서드를 탐색하다 `ExhaustedRetryException`(500)을 던집니다.

**발견 3: 낙관적 락 vs 비관적 락 트레이드오프**  
비관적 락은 데드락 없이 정합성을 보장하지만 응답시간이 약 **35배** 느려졌습니다 (2ms → 70ms).  
재고처럼 경합이 심한 도메인에서는 비관적 락이 안정적이지만, TPS 희생이 뒤따릅니다.

---

## 6. 트러블슈팅

### Issue #001 — 연관 엔티티 고유값 파생 필드 저장 문제

`Post.writer`, `Comment.writer`에 `Member.email`을 직접 복사 저장.  
회원 이메일 변경 시 모든 관련 레코드 일괄 업데이트 필요 → 정합성 문제.  
→ v1에서 파생 필드 제거, v2에서 ID Reference로 완전 정리 예정.

### Issue #002 — `Page<T>` Redis 캐싱 직렬화 실패

`PageImpl` 기본 생성자 없음 + `LocalDateTime` Jackson 미지원.  
우회책 누적보다 목록 캐싱 제거가 합리적이라 판단, Write-behind 조회수 캐싱만 유지.  
→ **캐싱 도입 전 "직렬화 가능한 타입인가"를 먼저 검토해야 한다**는 교훈.

### Issue #003 — 낙관적 락 + MySQL 데드락

상세 내용은 [5. 동시성 제어 실험](#5-동시성-제어-실험) 참조.

---

## 7. 실행 방법

### 요구사항

- Docker & Docker Compose
- Java 17

### 실행

```bash
# 저장소 클론
git clone https://github.com/your-username/shop.git
cd shop

# Docker로 MySQL + Redis 실행
docker-compose up -d mysql redis

# 애플리케이션 실행 (local 프로파일)
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 프로파일

| 프로파일 | DB | Redis |
|---|---|---|
| `test` | H2 (인메모리) | Embedded Redis |
| `local` | MySQL (Docker) | Redis (Docker) |
| `prod` | MySQL | Redis |

### 테스트 & 커버리지

```bash
# 테스트 실행 + JaCoCo 리포트 생성
./gradlew clean test jacocoTestReport

# 리포트 확인
open build/reports/jacoco/test/html/index.html
```

---

## 8. API 명세

애플리케이션 실행 후 Swagger UI에서 확인 가능합니다.

```
http://localhost:8080/swagger-ui/index.html
```

JWT 인증이 필요한 API는 로그인 후 발급된 `accessToken`을  
Swagger UI 우측 상단 **Authorize** 버튼에 `Bearer {token}` 형식으로 입력하세요.

### 주요 엔드포인트

| 도메인 | 메서드 | 경로 | 설명 |
|---|---|---|---|
| Auth | POST | `/auth/signup` | 회원가입 |
| Auth | POST | `/auth/login` | 로그인 |
| Auth | POST | `/auth/refresh` | 토큰 갱신 |
| Auth | POST | `/auth/logout` | 로그아웃 |
| Member | GET | `/members/me` | 내 정보 조회 |
| Member | PATCH | `/members/me` | 프로필 수정 |
| Member | DELETE | `/members/me` | 회원 탈퇴 |
| Post | GET | `/posts` | 게시글 목록 (페이징 + 검색) |
| Post | POST | `/posts` | 게시글 작성 (multipart) |
| Item | GET | `/items` | 상품 목록 (커서 페이징 + 검색) |
| Item | POST | `/items` | 상품 등록 |
| Order | POST | `/orders` | 주문 생성 |
| Order | PATCH | `/orders/{id}/status` | 주문 상태 변경 |
| Admin | PATCH | `/admin/orders/{id}/force-cancel` | 주문 강제 취소 |
| Admin | DELETE | `/admin/posts/{id}` | 게시글 강제 삭제 |

---

## v2 리팩터링 계획

현재 v1은 JPA의 Object Reference 방식으로 설계되어 있습니다.  
`shop-v2` 브랜치에서 아래 목표로 리팩터링을 진행할 예정입니다.

- **ID Reference 전환**: `Order.member` → `memberId`, `Post.member` → `memberId` 등
- **양방향 연관관계 제거**: `Member.posts`, `Member.orders` 역방향 컬렉션 삭제
- **서비스 계층 재설계**: 객체 그래프 탐색 → 명시적 Repository 조회
- **Aggregate 간 Cascade 제거**: 도메인 이벤트로 대체

v1과 v2의 설계 차이, 쿼리 수 변화, 트레이드오프는 별도 문서로 정리할 예정입니다.
