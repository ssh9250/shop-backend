ok) custom user detail 엔티티 대신 필수 정보만 + service 토스 객체도 수정
ok) tokenDto, jwtProperties 빈 파일 해결
x) authentication filter에 tokenProvider, redisTemplate 관련해서 filter config에서 빈 자동화
token provider validate token 검증 로직 재확인
auth service signup 로직 강화하기 (중복체크, 유효체크 api 추가 등/또는 member service에서 해야 할 작업인지 확인)
swaggerConfig에서 jwt 관련 설정 마무리
로그아웃 시 refresh token 삭제

ok) Refresh Token Rotation 로직 상세 이해

security config filter bean 중복 생성 문제 해결 issue에 기록

마지막 커밋 이후의 변경사항에 대한 내용을 이전 커밋 메시지들을 참고하여 COMMIT.md의 맨 마지막에 작성해주시고, 커밋은 하지마세요.


feat : 새로운 기능 추가
fix : 버그 수정
docs : 문서 수정
style : 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
refactor : 코드 리펙토링
test : 테스트 코드, 리펙토링 테스트 코드 추가
chore : 빌드 업무 수정, 패키지 매니저 수정

---

현재 저는 item, category가 양방향 many to many로 연관관계를 맺고 있습니다.
그런데 item 엔티티에만 category를 리스트로 저장할 수 있게끔 하고 category에는 item을 두지 않는다면
이것은 무슨 연관관계인가요? 그리고 특정 카테고리로 item 들을 검색할 때의 쿼리는 어떻게 나가게 되나요? 그 밖에도 이렇게 만들었을때의 문제점들에 대해 알려주세요.

오후 7:16
단방향 ManyToMany 관계가 됩니다.

Item 엔티티:

java
@ManyToMany
@JoinTable(name = "category_item",
joinColumns = @JoinColumn(name = "item_id"),
inverseJoinColumns = @JoinColumn(name = "category_id"))
private List<Category> categories;
특정 카테고리로 Item 검색 쿼리:

sql
SELECT i.* FROM item i
JOIN category_item ci ON i.item_id = ci.item_id
WHERE ci.category_id = ?
문제점들:

1. 성능 문제

Category → Item 조회 시 항상 조인 테이블 거쳐야 함
N+1 문제 발생 가능성
2. 코드 복잡성 증가

Repository에서 복잡한 JPQL/QueryDSL 필요
직관적이지 않은 조회 로직
3. 캐시 효율성 저하

Category 기준 Item 목록 캐싱 어려움
4. ORM 최적화 제한

JPA의 양방향 최적화 기능 활용 불가
권장: 성능상 이점이 명확하지 않다면 양방향 관계 유지가 좋습니다.

그러나 인터넷에서 양방향 다대다 관계는 지양하는 편이 좋다고 봤습니다. 서비스의 구조를 유지하기 위해서는 어쩔 수 없이 이대로 개발해야할까요?

오후 7:18
맞습니다! 양방향 다대다는 지양하는 것이 좋습니다. 더 나은 대안들이 있습니다.

1. 중간 엔티티 생성 (추천)

java
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
2. 단방향 + Repository 메서드

java
// ItemRepository
List<Item> findByCategoriesId(Long categoryId);

// CategoryService
public List<Item> getItemsByCategory(Long categoryId) {
return itemRepository.findByCategoriesId(categoryId);
}
3. QueryDSL 활용

java
public List<Item> findItemsByCategory(Long categoryId) {
return queryFactory
.selectFrom(item)
.join(item.categories, category)
.where(category.id.eq(categoryId))
.fetch();
}
권장: 중간 엔티티 방식이 가장 깔끔하고 확장성도 좋습니다. 단방향으로 가셔도 충분히 해결 가능합니다.

결론: 현재는 many to many 양방향 관계로 개발하되, 추후 querydsl을 활용하여 단방향으로 수정할 예정.