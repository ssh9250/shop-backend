# Entity 연관관계 및 N+1 문제 분석

## 1. Entity 연관관계 다이어그램

```
Member (1) ─── (N) Post
Member (1) ─── (N) Comment
Member (1) ─── (N) Order
Member (1) ─── (N) Item (seller)

Post (1) ─── (N) Comment
Post (1) ─── (N) PostFile

Order (1) ─── (N) OrderItem
OrderItem (N) ─── (1) Item

Item (1) ─── (N) CategoryItem
Category (1) ─── (N) CategoryItem
Category (1) ─── (N) Category (parent-child 자기참조)
```

---

## 2. Entity별 연관관계 상세

### Member
| 방향 | 대상 | 어노테이션 | Fetch | Cascade | orphanRemoval |
|------|------|-----------|-------|---------|----------------|
| 1:N | Post | @OneToMany(mappedBy="member") | LAZY(기본값) | ALL | true |
| 1:N | Comment | @OneToMany(mappedBy="member") | LAZY(기본값) | ALL | true |
| 1:N | Order | @OneToMany(mappedBy="member") | LAZY(기본값) | ALL | true |
| 1:N | Item | @OneToMany(mappedBy="member") | LAZY(기본값) | ALL | true |

**연관관계 주인**: Post, Comment, Order, Item (각 FK 보유)

---

### Post
| 방향 | 대상 | 어노테이션 | Fetch | Cascade | orphanRemoval |
|------|------|-----------|-------|---------|----------------|
| N:1 | Member | @ManyToOne | LAZY | - | - |
| 1:N | Comment | @OneToMany(mappedBy="post") | LAZY(기본값) | ALL | true |
| 1:N | PostFile | @OneToMany(mappedBy="post") | LAZY(기본값) | ALL | true |

**연관관계 주인**: Post (`member_id` FK 보유)

---

### Comment
| 방향 | 대상 | 어노테이션 | Fetch | Cascade | orphanRemoval |
|------|------|-----------|-------|---------|----------------|
| N:1 | Post | @ManyToOne | LAZY | - | - |
| N:1 | Member | @ManyToOne | LAZY | - | - |

**연관관계 주인**: Comment (`post_id`, `member_id` FK 보유)
**특이사항**: 소프트 삭제 구현 (`deleted` 플래그)

---

### PostFile
| 방향 | 대상 | 어노테이션 | Fetch | Cascade | orphanRemoval |
|------|------|-----------|-------|---------|----------------|
| N:1 | Post | @ManyToOne | LAZY | - | - |

**연관관계 주인**: PostFile (`post_id` FK 보유)

---

### Item
| 방향 | 대상 | 어노테이션 | Fetch | Cascade | orphanRemoval |
|------|------|-----------|-------|---------|----------------|
| N:1 | Member (seller) | @ManyToOne | LAZY | - | - |
| 1:N | CategoryItem | @OneToMany(mappedBy="item") | LAZY(기본값) | **없음** | false |

**연관관계 주인**: Item (`member_id` FK 보유)
**주의**: CategoryItem에 Cascade 미설정 — 의도한 설계인지 확인 필요

---

### Order
| 방향 | 대상 | 어노테이션 | Fetch | Cascade | orphanRemoval |
|------|------|-----------|-------|---------|----------------|
| N:1 | Member | @ManyToOne | LAZY | - | - |
| 1:N | OrderItem | @OneToMany(mappedBy="order") | LAZY(기본값) | ALL | true |

**연관관계 주인**: Order (`member_id` FK 보유)

---

### OrderItem
| 방향 | 대상 | 어노테이션 | Fetch | Cascade | orphanRemoval |
|------|------|-----------|-------|---------|----------------|
| N:1 | Order | @ManyToOne | LAZY | - | - |
| N:1 | Item | @ManyToOne | LAZY | - | - |

**연관관계 주인**: OrderItem (`order_id`, `item_id` FK 보유)

---

### Category
| 방향 | 대상 | 어노테이션 | Fetch | Cascade | orphanRemoval |
|------|------|-----------|-------|---------|----------------|
| N:1 | Category (parent) | @ManyToOne | LAZY | - | - |
| 1:N | Category (child) | @OneToMany(mappedBy="parent") | LAZY(기본값) | **없음** | false |
| 1:N | CategoryItem | @OneToMany(mappedBy="category") | LAZY(기본값) | **없음** | false |

**연관관계 주인**: Category (`parent_id` FK 보유, 자기참조)

---

### CategoryItem
| 방향 | 대상 | 어노테이션 | Fetch | Cascade | orphanRemoval |
|------|------|-----------|-------|---------|----------------|
| N:1 | Category | @ManyToOne | LAZY | - | - |
| N:1 | Item | @ManyToOne | LAZY | - | - |

**연관관계 주인**: CategoryItem (`category_id`, `item_id` FK 보유)
**역할**: Item ↔ Category 다대다 관계를 해소하는 중간 테이블 엔티티

---

## 3. N+1 문제 발생 지점

### 3.1 PostService — `getAllPosts()`

**위치**: `PostService.java`, `PostResponseDto.java`

```java
// PostService.getAllPosts()
return postRepository.findAll()             // 쿼리 1회: Post 목록 조회
        .stream()
        .map(PostResponseDto::from)          // Post 수(N)만큼 추가 쿼리
        .collect(Collectors.toList());

// PostResponseDto.from() 내부
.writer(post.getMember().getEmail())         // ← LAZY 로딩: Member 조회 N회
```

**발생 쿼리 수**: `1 + N` (Post가 100건이면 101회)
**해결 방법**: `PostRepository`에 `LEFT JOIN FETCH p.member` 추가

---

### 3.2 PostService — `updatePost()`, `deletePost()`

**위치**: `PostService.java`

```java
// updatePost() / deletePost()
post.getPostFiles().forEach(file -> {        // ← LAZY 로딩: PostFile 컬렉션 조회 1회
    fileStorageService.deleteFile(file.getStoredFileName());
});
```

**발생 쿼리 수**: `+1` (updatePost/deletePost 호출 시 PostFile 컬렉션 별도 조회)
**해결 방법**: `PostRepository`에 `LEFT JOIN FETCH p.postFiles` 추가

**추가 문제 — 중복 Member 조회**:
```java
// updatePost() / deletePost() 내부 흐름
memberRepository.findById(memberId)          // 1회 조회
postRepository.findById(postId)
validatePostAccess(memberId, post)
  └─ memberRepository.findById(memberId)     // 2회 조회 (중복!)
```

---

### 3.3 OrderService / OrderAdminService — 주문 조회 전반

**위치**: `OrderService.java`, `OrderAdminService.java`, `OrderResponseDto.java`, `OrderItemResponseDto.java`

```java
// OrderResponseDto.from() 내부
order.getOrderItems()                         // ← LAZY 로딩: OrderItem 컬렉션 N회
    .stream()
    .map(OrderItemResponseDto::from)
    ...
order.getMember().getEmail()                  // ← LAZY 로딩: Member N회
order.getMember().getNickname()               // (동일 세션이면 1회로 합산)

// OrderItemResponseDto.from() 내부
orderItem.getOrder().getId()                  // ← LAZY 로딩: Order 재조회 N*M회
```

**영향 메서드**:
- `OrderAdminService.getAllOrders()` — `findAll()` 사용으로 전체 주문 대상
- `OrderAdminService.getOrdersByStatus()`
- `OrderAdminService.getOrdersByMember()`
- `OrderService.getOrdersByStatus()`
- `OrderService.findOrderById()` — 단건이므로 상대적으로 낮음

**발생 쿼리 수**: `1 + 2N + N*M`
(Order가 10건, 각 Order에 OrderItem 5건이면 최대 61회)

**해결 방법**:
```java
// OrderRepository에 FETCH JOIN 추가
@Query("SELECT o FROM Order o " +
       "LEFT JOIN FETCH o.member " +
       "LEFT JOIN FETCH o.orderItems oi " +
       "WHERE o.member.id = :memberId")
List<Order> findByMemberIdWithFetch(@Param("memberId") Long memberId);
```

**추가 문제 — OrderItemResponseDto에서 Order 역방향 재조회**:
```java
// orderItem.getOrder()는 이미 로딩된 Order를 재참조하는데
// 영속성 컨텍스트 캐시 덕분에 쿼리가 발생하지 않을 수 있으나,
// 트랜잭션 범위 밖에서 호출될 경우 LazyInitializationException 위험
orderItem.getOrder().getId()  // Order → OrderItem → Order 순환 참조
```

---

### 3.4 CommentService — `getCommentsByPostId()`

**위치**: `CommentRepositoryImpl.java`

```java
// CommentRepositoryImpl.findByPostId()
return queryFactory
        .selectFrom(comment)
        .where(comment.post.id.eq(postId))
        .orderBy(comment.createdAt.desc())
        .fetch();                              // ← FETCH JOIN 없이 Comment만 조회
```

현재 `CommentResponseDto.from()`이 Comment 엔티티 자체 필드(`writer`, `content`)만 접근하므로 N+1이 직접 발생하지는 않습니다.
단, 향후 `comment.getMember()` 또는 `comment.getPost()`를 DTO에 추가하면 즉시 N+1이 발생합니다.

**예방적 해결 방법**:
```java
return queryFactory
        .selectFrom(comment)
        .leftJoin(comment.member).fetchJoin()  // 선제적으로 추가 권장
        .where(comment.post.id.eq(postId))
        .orderBy(comment.createdAt.desc())
        .fetch();
```

---

### 3.5 ItemService — `validateItemAccess()`

**위치**: `ItemService.java`

```java
private void validateItemAccess(Item item, Long memberId) {
    Member member = memberRepository.findById(memberId)   // ← Member 조회 1회 (불필요)
            .orElseThrow(...);

    if (!item.getSeller().getId().equals(member.getId())) // ← LAZY 로딩: seller 조회 1회
        throw new AccessDeniedException(...);
}
```

**발생 쿼리 수**: `+2` (validateItemAccess 호출마다)
**해결 방법**: `item.getSeller().getId()`를 `item.getSellerId()`처럼 FK 값을 직접 비교하거나, 이미 로딩된 memberId와 직접 비교

---

### 3.6 Order 엔티티 도메인 메서드 — `cancel()`, `forceCancel()`

**위치**: `Order.java`

```java
public void cancel() {
    for (OrderItem orderItem : orderItems) {  // ← orderItems LAZY 컬렉션 초기화
        orderItem.cancel();                    //    → Item 재고 복구 시 item 조회 가능성
    }
}
```

**위험 시나리오**: 서비스에서 `Order`를 조회한 뒤 `cancel()`을 호출할 때, `orderItems` 컬렉션이 초기화되어 있지 않으면 추가 쿼리 발생
**해결 방법**: `OrderService`에서 cancel 전에 `orderItems`가 포함된 쿼리로 Order 조회

---

## 4. N+1 문제 요약표

| 발생 위치 | 메서드 | 심각도 | 발생 패턴 | Fetch Join 존재 |
|-----------|--------|--------|-----------|----------------|
| OrderAdminService | getAllOrders() | 매우 높음 | 1 + 2N + N*M | 없음 |
| OrderAdminService | getOrdersByStatus() | 매우 높음 | 1 + 2N + N*M | 없음 |
| OrderAdminService | getOrdersByMember() | 매우 높음 | 1 + 2N + N*M | 없음 |
| OrderService | getOrdersByStatus() | 매우 높음 | 1 + 2N + N*M | 없음 |
| PostService | getAllPosts() | 높음 | 1 + N | 없음 |
| PostService | updatePost() | 중간 | +1 (PostFile) | 없음 |
| PostService | deletePost() | 중간 | +1 (PostFile) | 없음 |
| Order (도메인) | cancel(), forceCancel() | 중간 | orderItems LAZY 초기화 | 없음 |
| CommentRepositoryImpl | findByPostId() | 낮음 (잠재적) | 향후 필드 추가 시 발생 | 없음 |
| ItemService | validateItemAccess() | 낮음 | +2 (중복 조회) | 없음 |

---

## 5. 중복 조회 문제 요약

`validateXxxAccess()` 패턴에서 Member를 서비스 메서드와 검증 메서드 양쪽에서 중복 조회하는 문제가 반복됩니다.

| 서비스 | 메서드 | 중복 횟수 |
|--------|--------|-----------|
| PostService | updatePost(), deletePost() | Member 2~3회 |
| CommentService | updateComment(), deleteComment() | Member 2~3회 |
| OrderService | acceptOrder(), startDelivery() 등 | Member 2~3회 |
| ItemService | updateItem(), deleteItem() | Member 2회 |

**공통 해결 방향**: 서비스 메서드에서 Member를 1회 조회한 뒤 검증 메서드에 파라미터로 전달

---

## 6. 전체 Fetch 전략 현황

| 연관관계 | Fetch 전략 | Repository Fetch Join |
|----------|-----------|----------------------|
| Post → Member | LAZY | 없음 |
| Post → Comment | LAZY (기본값) | 없음 |
| Post → PostFile | LAZY (기본값) | 없음 |
| Order → Member | LAZY | 없음 |
| Order → OrderItem | LAZY (기본값) | 없음 |
| OrderItem → Item | LAZY | 없음 |
| Comment → Post | LAZY | 없음 |
| Comment → Member | LAZY | 없음 |
| Item → CategoryItem | LAZY (기본값) | 없음 |
| Category → CategoryItem | LAZY (기본값) | 없음 |
| Category → Category(child) | LAZY (기본값) | 없음 |

전체 연관관계가 LAZY 전략을 사용하고 있으며, 어떠한 Repository에도 Fetch Join이나 `@EntityGraph`가 적용되어 있지 않습니다.