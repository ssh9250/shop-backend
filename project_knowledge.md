# shop - Spring Boot C2C 중고거래 플랫폼 프로젝트 지식

## 1. 프로젝트 개요

**기술 스택:** Java 17, Spring Boot 3.3.5, Spring Security, JWT, Spring Data JPA, QueryDSL 5.0.0, Redis, H2 Database
**도메인:** 악기 중심 C2C 중고거래 플랫폼 (게시글/댓글, 회원 관리, AI 악보 변환 포함)
**아키텍처:** DDD(Domain-Driven Design) 기반 계층형 아키텍처
**답변 방식:** 간결하고 실용적으로, 개념 설명 + 코드 예시 포함

---

## 2. 패키지 구조

```
src/main/java/com/study/shop/
├── ShopApplication.java
├── admin/
│   ├── controller/  OrderAdminController, PostAdminController
│   └── service/     OrderAdminService, PostAdminService
├── domain/
│   ├── auth/        controller, dto, service
│   ├── member/      controller, dto, entity, exception, repository, service
│   ├── post/        controller, dto, entity, exception, repository, service
│   ├── comment/     controller, dto, entity, exception, repository, service
│   ├── Item/        controller, dto, entity, exception, repository, service
│   ├── order/       controller, dto, entity, exception, repository, service
│   └── category/    entity
├── global/
│   ├── config/      FilterConfig, JpaAuditingConfig, QueryDslConfig, RedisConfig, SwaggerConfig, WebConfig
│   ├── dto/         PageResponse
│   ├── enums/       RoleType, ItemStatus, OrderStatus, InstrumentCategory
│   ├── exception/   CustomException, ErrorCode, GlobalExceptionHandler
│   ├── response/    ApiResponse
│   └── util/        BaseEntity, BaseTimeEntity, JpaBaseEntity, JwtUtils
├── infrastructure/
│   ├── ai/          MusicScoreClient, MusicScoreConverter
│   └── external/    StorageService
└── security/
    ├── auth/        CustomUserDetails, CustomUserDetailsService
    ├── config/      SecurityConfig
    ├── dto/         RefreshRequestDto, RefreshResponseDto
    ├── exception/   ExpiredTokenException, InvalidTokenException, RefreshTokenNotFoundException, RefreshTokenMismatchException, UnauthorizedException
    ├── jwt/         JwtTokenProvider, JwtAuthenticationFilter, JwtExceptionFilter
    ├── refresh/     RefreshTokenRepository, RefreshTokenService
    └── util/        SecurityResponseUtil
```

---

## 3. 엔티티 상세 구조

### Member
```java
// 필드: id(PK), email(unique, len=50), password(len=200), nickname(len=50), phone(len=50), address, role(RoleType)
// 상속: BaseTimeEntity (createdAt, updatedAt)

// 연관관계
@OneToMany(mappedBy="member", cascade=ALL, orphanRemoval=true) List<Post> posts;
@OneToMany(mappedBy="member", cascade=ALL, orphanRemoval=true) List<Comment> comments;
@OneToMany(mappedBy="member", cascade=ALL, orphanRemoval=true) List<Order> orders;
@OneToMany(mappedBy="seller",  cascade=ALL, orphanRemoval=true) List<Item> items;

// 도메인 메서드
updateProfile(nickname, phone, address)
updatePassword(newPassword)
addPost(post) / removePost(post)       // 양방향 동기화
addComment(comment) / removeComment(comment)
```

### Post
```java
// 필드: id(PK), title, content(TEXT)
// 상속: BaseTimeEntity

// 연관관계
@ManyToOne(fetch=LAZY) Member member;
@OneToMany(mappedBy="post", cascade=ALL, orphanRemoval=true) List<Comment> comments;
@OneToMany(mappedBy="post", cascade=ALL, orphanRemoval=true) List<PostFile> postFiles;

// 도메인 메서드
static Post create(title, content, member)   // 정적 팩토리
update(title, content)
addPostFile(postFile) / removePostFile(postFile)
addComment(comment) / removeComment(comment)
```

### PostFile
```java
// 필드: id(PK), originalFileName, storedFileName(UUID+원본명), filePath, fileSize(Long)
// 연관관계
@ManyToOne(fetch=LAZY) Post post;
void assignPost(Post post)  // package-private
```

### Comment
```java
// 필드: id(PK), writer, content(TEXT), deleted(boolean, default=false)
// 상속: BaseTimeEntity

// 연관관계
@ManyToOne(fetch=LAZY) Post post;
@ManyToOne(fetch=LAZY) Member member;

// 도메인 메서드
static Comment create(member, post, content)  // 정적 팩토리 - assignPost/assignMember 내부 호출
void assignPost(Post post)     // package-private, 양방향 동기화
void assignMember(Member member) // package-private
update(content)
delete()   // soft delete: deleted = true
```

### Item
```java
// 필드: id(PK), name, description, stock(int), price(int), used(boolean), itemStatus(ItemStatus)
// 상속: BaseTimeEntity

// 연관관계
@ManyToOne(fetch=LAZY) Member seller;
@OneToMany(mappedBy="item") List<CategoryItem> categoryItems;

// 도메인 메서드
static Item create(seller, name, description, stock, price, used)  // itemStatus = ON_SALE
update(UpdateItemRequestDto)
validateOrderable()   // itemStatus != ON_SALE 이면 예외
addStock(quantity) / removeStock(quantity)
addCategory(category) / removeCategory(category)
```

### Order
```java
// 필드: id(PK), orderDate(LocalDateTime), totalPrice(int), orderStatus(default=PENDING), address
// 연관관계
@ManyToOne(fetch=LAZY) Member member;
@OneToMany(mappedBy="order", cascade=ALL, orphanRemoval=true) List<OrderItem> orderItems;

// 도메인 메서드 - 상태 전이
static Order create(member, address)
addOrderItem(orderItem)           // totalPrice 자동 재계산
accept()       // PENDING → ORDERED
startDelivery() // ORDERED → IN_DELIVERY
complete()     // IN_DELIVERY → COMPLETED
cancel()       // PENDING → CANCELLED, OrderItem.cancel() 연쇄 호출
forceCancel()  // 관리자용 강제 취소
validateStatusTransition(expected, next)
```

### OrderItem
```java
// 필드: id(PK), quantity(Integer), price(int, 주문 시점 가격)
// 연관관계
@ManyToOne(fetch=LAZY) Order order;
@ManyToOne(fetch=LAZY) Item item;

// 도메인 메서드
static OrderItem create(item, quantity)  // item.validateOrderable() + stock 확인 + stock 감소
cancel()           // item stock 복구
getTotalPrice()    // price * quantity
```

### Category / CategoryItem
```java
// Category 필드: id(PK), name
// 연관관계
@ManyToOne self-reference parent; @OneToMany children;
@OneToMany List<CategoryItem> categoryItems;
addChild/removeChild, addItem/removeItem

// CategoryItem: category(ManyToOne) + item(ManyToOne) 연결 테이블 엔티티
```

---

## 4. API 엔드포인트

### AuthController - `/api/auth`
| HTTP | Path | 설명 | Request | Response |
|------|------|------|---------|---------|
| POST | `/signup` | 회원가입 | SignupRequestDto | ApiResponse\<Void\> |
| POST | `/login` | 로그인 | LoginRequestDto | ApiResponse\<LoginResponseDto\> |
| POST | `/refresh` | 토큰 갱신 | RefreshRequestDto | ApiResponse\<RefreshResponseDto\> |
| POST | `/logout` | 로그아웃 | @AuthenticationPrincipal | ApiResponse\<Void\> |

### MemberController - `/api/members`
| HTTP | Path | 설명 | 인증 |
|------|------|------|------|
| GET | `/me` | 내 정보 조회 | 필요 |
| PATCH | `/profile` | 프로필 수정 (nickname, phone, address) | 필요 |
| PATCH | `/password` | 비밀번호 변경 | 필요 |
| DELETE | `` | 회원 탈퇴 | 필요 |

### PostController - `/api/posts`
| HTTP | Path | 설명 | 인증 |
|------|------|------|------|
| POST | `/` | 게시글 작성 (`multipart/form-data`, @RequestPart) | 필요 |
| GET | `/` | 전체 게시글 조회 | 불필요 |
| GET | `/{id}` | 게시글 단건 조회 | 불필요 |
| PATCH | `/{id}` | 게시글 수정 (multipart) | 필요 |
| DELETE | `/{id}` | 게시글 삭제 | 필요 |

### CommentController - `/api/comments`
| HTTP | Path | 설명 | 인증 |
|------|------|------|------|
| POST | `/` | 댓글 작성 | 필요 |
| GET | `/` | 게시글 댓글 조회 (`?postId=`) | 불필요 |
| PATCH | `/{commentId}` | 댓글 수정 | 필요 |
| DELETE | `/{id}` | 댓글 소프트 삭제 | 필요 |

### ItemController - `/api/items`
| HTTP | Path | 설명 | 인증 |
|------|------|------|------|
| POST | `/` | 상품 생성 | 필요 |
| GET | `/` | 전체 상품 조회 (ON_SALE만) | 불필요 |
| GET | `/{id}` | 상품 단건 조회 | 불필요 |
| GET | `/me` | 내 상품 목록 | 필요 |
| PUT | `/{itemId}` | 상품 수정 (ON_SALE만) | 필요 |
| DELETE | `/{itemId}` | 상품 삭제 (ON_SALE만) | 필요 |

### OrderController - `/api/order`
| HTTP | Path | 설명 |
|------|------|------|
| POST | `/` | 주문 생성 |
| GET | `/{id}` | 주문 단건 조회 |
| GET | `/status/{status}` | 상태별 주문 조회 |
| PATCH | `/{id}/accept` | 주문 수락 (PENDING→ORDERED) |
| PATCH | `/{id}/delivery` | 배송 시작 (ORDERED→IN_DELIVERY) |
| PATCH | `/{id}/complete` | 배송 완료 (IN_DELIVERY→COMPLETED) |
| DELETE | `/{id}` | 주문 취소 |

### OrderAdminController - `/admin/orders` (ROLE_ADMIN)
| HTTP | Path | 설명 |
|------|------|------|
| GET | `/` | 전체 주문 조회 |
| GET | `/{id}` | 주문 단건 조회 |
| GET | `/status/{status}` | 상태별 조회 |
| GET | `/member/{memberId}` | 회원별 조회 |
| PATCH | `/{id}/accept` | 주문 수락 |
| PATCH | `/{id}/delivery` | 배송 시작 |
| PATCH | `/{id}/complete` | 배송 완료 |
| DELETE | `/{id}` | 강제 취소 |

---

## 5. 서비스 계층

### AuthService
- `signup(dto)` - 회원가입
- `login(dto)` - 로그인, Access/Refresh Token 생성
- `refresh(token)` - Token 갱신 (Refresh Token Rotation)
- `logout(accessToken, email)` - Redis 블랙리스트 등록

### MemberService
- `signup(dto)` - 이메일/닉네임 중복 검증, 비밀번호 BCrypt 암호화
- `getMemberById(id)` / `getMemberByEmail(email)`
- `updateProfile(memberId, dto)` / `updatePassword(memberId, dto)`
- `deleteMember(memberId)` - cascade 삭제

### PostService
- `createPost(memberId, dto, files)` - FileStorageService로 파일 저장
- `getAllPosts()` / `getPostById(id)`
- `updatePost(memberId, postId, dto, files)` - 권한 검증, 기존 파일 삭제 후 교체
- `deletePost(memberId, postId)` - 권한 검증, 파일 삭제
- `validatePostAccess(memberId, post)` - 작성자 권한 검증

### FileStorageService
- `storeFile(MultipartFile)` - UUID 기반 파일명으로 로컬 저장 (`/uploads/posts`)
- `deleteFile(storedFileName)` - 파일 삭제

### CommentService
- `createComment(memberId, dto)` - `Comment.create()` 호출
- `getCommentsByPostId(postId)` - QueryDSL (deleted=false 필터링)
- `updateComment(memberId, commentId, dto)` - 권한 검증
- `deleteComment(memberId, commentId)` - soft delete

### ItemService
- `createItem(memberId, dto)` - `Item.create()` 호출
- `getAllItems()` - ON_SALE 상태만 반환
- `getItemById(itemId)` / `getItemsByMemberId(memberId)`
- `updateItem(memberId, itemId, dto)` - ON_SALE 상태 검증 + 판매자 권한 검증
- `deleteItem(memberId, itemId)` - ON_SALE 상태 검증 + 판매자 권한 검증

### OrderService
- `createOrder(memberId, dto)` - OrderItem.create() 호출 (stock 검증 포함)
- `findOrderById(memberId, orderId)` - 권한 검증
- `getOrdersByStatus(memberId, status)` - 상태별 조회
- `acceptOrder / startDelivery / completeOrder(memberId, orderId)` - 상태 전이
- `cancelOrder(memberId, orderId)` - 취소, stock 복구
- `validateOrderAccess(order, memberId)` - 구매자 권한 검증

---

## 6. 리포지토리

### MemberRepository
```java
Optional<Member> findMemberByEmail(String email);
boolean existsByEmail(String email);
boolean existsByNickname(String nickname);
```

### CommentRepository (QueryDSL 적용)
```java
// JPQL
@Query("select c from Comment c where c.member.id = :memberId and c.deleted = false")
List<Comment> findActiveCommentByMemberId(Long memberId);

// QueryDSL (CommentRepositoryImpl)
// findByPostId: post.id = ? AND deleted = false ORDER BY createdAt DESC
```

### ItemRepository
```java
List<Item> findBySellerId(Long sellerId);
```

### OrderRepository
```java
List<Order> findByOrderStatus(OrderStatus orderStatus);
List<Order> findByMemberId(Long memberId);
List<Order> findByStatusAndMemberId(OrderStatus orderStatus, Long memberId);
```

---

## 7. 보안 (Security)

### SecurityConfig 핵심 설정
- CSRF 비활성화 (stateless)
- Session: STATELESS
- Password Encoder: BCryptPasswordEncoder
- Authentication Provider: DaoAuthenticationProvider
- **필터 체인:** JwtExceptionFilter → JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter
- **CORS:** `http://localhost:3000`, `http://localhost:5173` 허용, GET/POST/PUT/DELETE/OPTIONS

**Authorization 규칙:**
```
PUBLIC:  /api/auth/login, /api/auth/refresh, /swagger-ui/**, /v3/api-docs/**
ADMIN:   /admin/**  → ROLE_ADMIN
기타:    authenticated()
```

### JWT 설정
```yaml
jwt:
  access-expiration-ms: 1800000     # 30분
  refresh-expiration-ms: 1209600000 # 14일
  secret: (application-local.yml 관리)
```
- **알고리즘:** HS256
- **Claim:** email (subject)

### JwtTokenProvider 메서드
- `createAccessToken(email)` / `createRefreshToken(email)`
- `validateToken(token)` / `getEmail(token)` / `getAuthentication(token)`
- `getExpiration(token)` - 남은 시간(ms)

### JwtAuthenticationFilter 동작
1. Authorization 헤더에서 "Bearer " 토큰 추출
2. Redis 블랙리스트 확인 (`blacklist:{token}`)
3. 토큰 유효성 검증
4. SecurityContext에 Authentication 등록

### Refresh Token (Redis)
```java
// 저장 키: "refresh:{email}"
RefreshTokenRepository.store(email, token, ttl)
RefreshTokenRepository.findByEmail(email)
RefreshTokenRepository.delete(email)
```

**RefreshTokenService:**
- `createAndStoreRefreshToken(email)` - 생성 및 Redis 저장
- `validateRefreshToken(email, token)` - 검증
- `rotateRefreshToken(email, oldToken)` - 새 토큰 발급 (Rotation)
- `removeRefreshToken(email)` - 삭제

### CustomUserDetails (UserDetails 구현)
```java
// 필드: memberId(Long), email, password, nickname, role(RoleType)
getAuthorities()  → "ROLE_USER" 또는 "ROLE_ADMIN"
getUsername()     → email
```

---

## 8. 전역 예외 처리

### ErrorCode (26개)
| 카테고리 | 코드 |
|---------|------|
| Member | MEMBER_NOT_FOUND, DUPLICATE_EMAIL, DUPLICATE_NICKNAME |
| Post | POST_NOT_FOUND |
| Comment | COMMENT_NOT_FOUND |
| Item | ITEM_NOT_FOUND, STOCK_NOT_ENOUGH |
| Order | ORDER_NOT_FOUND |
| Security | UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN, ACCESS_DENIED |
| RefreshToken | REFRESH_TOKEN_NOT_FOUND, REFRESH_TOKEN_MISMATCH |
| 기타 | INVALID_REQUEST |

### GlobalExceptionHandler
| 예외 | 상태코드 |
|------|---------|
| CustomException | code.status |
| MethodArgumentNotValidException | 400 |
| BadCredentialsException / AuthenticationException | 401 |
| Exception (기타) | 500 |

### ApiResponse\<T\>
```java
class ApiResponse<T> {
    boolean success;
    T data;
    String message;

    static <T> ApiResponse<T> success(T data)
    static <T> ApiResponse<T> success(T data, String message)
    static <T> ApiResponse<T> fail(String message)
}
```

---

## 9. 설정 파일

### application.yml
```yaml
spring:
  application.name: shop
  jpa:
    open-in-view: false
    properties.hibernate.format_sql: true
  profiles.active: local

logging:
  level:
    root: info
    org.hibernate.SQL: warn
    org.springframework: warn
    com.study.cruisin: debug

springdoc:
  api-docs.path: /v3/api-docs
  swagger-ui.path: /swagger-ui.html

jwt:
  access-expiration-ms: 1800000
  refresh-expiration-ms: 1209600000

file.upload-dir: /uploads/posts
```

**프로파일:** application-local.yml, application-dev.yml, application-prod.yml

### build.gradle 주요 의존성
```gradle
// QueryDSL
implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'

// Spring Boot Starters
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-validation'
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0'

// JWT
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'

// Lombok, H2, Test
compileOnly/annotationProcessor lombok
runtimeOnly h2
testImplementation 'com.github.codemonstur:embedded-redis:1.4.3'
```

---

## 10. DTO 정리

### 인증
| DTO | 필드 |
|-----|------|
| SignupRequestDto | email, password, nickname, phone, address |
| LoginRequestDto | email, password |
| LoginResponseDto | memberId, email, accessToken, refreshToken |
| RefreshRequestDto | refreshToken |
| RefreshResponseDto | accessToken, refreshToken |

### 회원
| DTO | 필드 |
|-----|------|
| MemberResponseDto | id, email, nickname, phone, address, role |
| UpdateProfileRequestDto | nickname, phone, address |
| ChangePasswordRequestDto | password |

### 게시글
| DTO | 필드 |
|-----|------|
| CreatePostRequestDto | title, content |
| UpdatePostRequestDto | title, content |
| PostResponseDto | id, title, content, writer, createdAt, updatedAt |

### 댓글
| DTO | 필드 |
|-----|------|
| CreateCommentRequestDto | postId, content |
| UpdateCommentRequestDto | content |
| CommentResponseDto | id, writer, content, createdAt, updatedAt |

### 상품
| DTO | 필드 |
|-----|------|
| CreateItemRequestDto | name, description, stock, price, used |
| UpdateItemRequestDto | name, description, price, used, status |
| ItemResponseDto | id, name, description, price, used, status |

### 주문
| DTO | 필드 |
|-----|------|
| CreateOrderRequestDto | orderItems: List\<CreateOrderItemRequestDto\>, address |
| CreateOrderItemRequestDto | itemId, quantity |
| OrderResponseDto | orderId, memberEmail, memberNickname, orderItemDtoList, orderStatus, totalPrice, orderDate, address |
| OrderItemResponseDto | orderId, orderItemId, quantity, price |

---

## 11. 열거형

| 이름 | 값 |
|------|-----|
| RoleType | USER, ADMIN |
| ItemStatus | ON_SALE, RESERVED, SOLD_OUT, HIDDEN |
| OrderStatus | PENDING, ORDERED, IN_DELIVERY, COMPLETED, CANCELLED |
| InstrumentCategory | 정의만 있음 (미구현) |

---

## 12. 도메인 설계 원칙

### 연관관계 편의 메서드
- **행위의 주체** 엔티티에 작성 (Comment → assignPost/assignMember)
- Builder에서 연관관계 필드 제거 → assign 메서드로만 설정
- 패키지 프라이빗으로 캡슐화

### 정적 팩토리 메서드 (create)
- 엔티티 생성은 `create()` 도메인 메서드로 통일
- 생성 시 필요한 연관관계 설정 및 검증을 내부에서 처리

### 비즈니스 검증 로직 위치
- **도메인 메서드:** 해당 엔티티 스스로 알 수 있는 규칙 (`validateOrderable`, 상태 전이 검증)
- **Service:** 외부 엔티티 조회가 필요한 검증 (권한 체크, 중복 확인)

### Cascade 및 소프트 삭제
- 1:N에서 부모 삭제 시 자식도 삭제 → `CascadeType.ALL + orphanRemoval=true`
- 파일(PostFile)은 cascade 대상이 아님 → 수동 삭제 필요
- Comment는 소프트 삭제 (`deleted` 필드)

### Aggregate 경계
| Aggregate Root | 포함 엔티티 |
|---|---|
| Order | OrderItem |
| Post | Comment, PostFile |
| Item | CategoryItem |
| Member | - |

- Aggregate Root만 Repository 보유
- 내부 엔티티는 Root를 통해서만 조작

### 트랜잭션 관리
- `@Transactional(readOnly=true)` - 조회 메서드
- `@Transactional` - 변경 메서드
- 더티 체킹으로 update 시 `save()` 불필요

### URL 설계
- 본인 리소스 조회: URL에 memberId 노출 금지 → JWT에서 추출
- 파일 업로드: `multipart/form-data`, `@RequestPart` 사용

---

## 13. 요청 처리 흐름

```
HTTP Request
  ↓
[JwtExceptionFilter] → [JwtAuthenticationFilter]  (토큰 검증, SecurityContext 설정)
  ↓
[Controller]  (@AuthenticationPrincipal CustomUserDetails로 인증 정보 주입)
  ↓
[Service]  (비즈니스 로직, 권한 검증, @Transactional)
  ↓
[Repository]  (JPA / QueryDSL)
  ↓
[Entity]  (정적 팩토리, 도메인 메서드, 상태 전이)
  ↓
[H2 Database]

외부 저장소
├── Redis: Refresh Token ("refresh:{email}"), Blacklist ("blacklist:{token}")
└── 파일 시스템: /uploads/posts

[GlobalExceptionHandler] → ApiResponse<T> (JSON 응답 통일)
```

---

## 14. 미완성/추가예정 기능, TODO 항목

- **PostAdminController/Service** - 스켈레톤 상태 (미구현)
- **AI 통합** - MusicScoreConverter, MusicScoreClient 인터페이스만 정의
- **파일 저장소** - 로컬 파일시스템 (프로덕션 시 S3 전환 필요)
- **InstrumentCategory** - 열거형 정의만 있고 사용 안 됨 (안할 가능성 큼)
- **QueryDSL** - Comment만 적용, 향후 N+1 발생 도메인에 확대 필요
- docker
- 페이징 기능