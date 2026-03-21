ok) custom user detail 엔티티 대신 필수 정보만 + service 토스 객체도 수정
ok) tokenDto, jwtProperties 빈 파일 해결
x) authentication filter에 tokenProvider, redisTemplate 관련해서 filter config에서 빈 자동화
token provider validate token 검증 로직 재확인
auth service signup 로직 강화하기 (중복체크, 유효체크 api 추가 등/또는 member service에서 해야 할 작업인지 확인)
swaggerConfig에서 jwt 관련 설정 마무리
로그아웃 시 refresh token 삭제

ok) Refresh Token Rotation 로직 상세 이해

1. QueryDSL 동적 검색 + 페이징
   면접 포인트: "왜 JPQL 안 쓰고 QueryDSL?"
   → 타입 안전성, 컴파일 타임 오류 감지, 동적 조건 조합
   → count 쿼리 분리로 성능 최적화까지 설명 가능
2. Cursor 기반 페이징 (상품/피드)
   면접 포인트: "Offset과 Cursor의 차이?"
   → 대용량 데이터에서 offset의 성능 저하 (LIMIT 10000, 20)
   → 복합 커서 (createdAt + id) 로 안정성 확보
   → 실무에서 무한스크롤은 거의 Cursor 사용
3. Redis 활용 다양화
   현재: Refresh Token + Blacklist
   추가: 인기글 캐싱, 조회수 증가 (Write-behind)
   면접 포인트: "Redis를 어떤 문제를 해결하기 위해 썼나요?"
   → DB 부하 분산, 실시간 카운터, TTL 기반 캐시 전략


// -Dsun.java2d.metal=false

security config filter bean 중복 생성 문제 해결 issue에 기록

마지막 커밋 이후의 변경사항에 대한 작업 내용을 이전 커밋 메시지들을 참고하여 가능한 간단하게 COMMIT.md의 맨 마지막에 작성해주시고, 커밋은 하지마세요.
현재까지 진행된 내용을 project_knowledge.md에 반영하여 업데이트해주세요.


feat : 새로운 기능 추가
fix : 버그 수정
docs : 문서 수정
style : 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
refactor : 코드 리펙토링
test : 테스트 코드, 리펙토링 테스트 코드 추가
chore : 빌드 업무 수정, 패키지 매니저 수정

---

Feat: Item Cursor 페이징 QueryDSL 구현 및 BaseTimeEntity softDelete 추가
- ItemRepositoryImpl: findByCondition() Cursor 기반 Slice 구현 (복합 커서 createdAt + id, DTO Projection)
- ItemRepositoryCustom: 시그니처 변경 (lastId → lastCreatedAt + lastId, 반환 타입 Slice<Item> → Slice<ItemListDto>)
- ItemSearchConditionDto: 필드 재설계 (Post 검색 조건 → Item 특화 조건: content, stock, used, minPrice, maxPrice)
- ItemListDto: 목록 조회용 DTO 추가 (id, name, stock, price, used, seller, createTime)
- ItemDetailDto: 단건 조회용 DTO 추가 (빈 클래스, 구현 예정)
- BaseTimeEntity: deletedAt 필드 및 softDelete()/isDeleted() 메서드 추가
- Member: BaseEntity 상속으로 전환, deleted 필드 제거 (BaseTimeEntity의 deletedAt으로 통일)
